package com.example.hardwarepulse

import androidx.recyclerview.widget.GridLayoutManager
import android.os.Build
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

// MainActivity 是 App 启动的第一个页面，它是所有操作的总指挥。
class MainActivity : AppCompatActivity() {

    // `lateinit var`：留出位置给数据源和搬运工，等 onCreate 准备好再放进来
    private lateinit var hardwareItems: MutableList<HardwareItem>
    private lateinit var adapter: HardwareAdapter
    private lateinit var tvTimer: TextView

    private var secondsElapsed = 0

    // `handler`：像是一个滴答作响的时钟，负责每秒一次的循环任务
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    // 核心任务：同时抓取电池和内存，刷新 6 个集装箱
    private fun refreshHardwareData() {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // 1. 注册临时广播接收器以获取系统电池状态信息，返回包含电量、温度、电压等数据的 Intent 对象
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val volt = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val chargingText = if (isCharging) "正在充电" else "未充电"
        val currentNow = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val absCurrent = Math.abs(currentNow).toInt()

        // 功耗公式：P = U * I，最后除以 1000 得到毫瓦 (mW)
        val power = (volt.toDouble() * absCurrent) / 1000.0

        // 2. 抓取内存信息。系统返回的是 Byte，需要转换：
        // 换算公式：$$MB = \frac{Bytes}{1024 \times 1024}$$
        // 获取系统内存信息：创建 MemoryInfo 对象并从 ActivityManager 中读取总内存和可用内存数据，
        // 将字节单位转换为 MB，计算已用内存及其占总内存的百分比
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        val totalMem = memoryInfo.totalMem / (1024 * 1024)
        val usedMem = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
        val memPercent =
            if (totalMem > 0L) ((usedMem.toDouble() / totalMem.toDouble()) * 100).toInt() else 0

        // 3. 将新数据填入 6 个集装箱（注意顺序必须与初始化时一致）
        hardwareItems[0].apply {
            value = "$level%"
            extraInfo = "状态：$chargingText" // 拼接成最终显示的字符串
            progress = level
        }
        hardwareItems[1].apply {
            value = "$memPercent%"; extraInfo = "${usedMem}MB / ${totalMem}MB"; progress =
            memPercent
        }
        hardwareItems[2].apply {
            value = "${temp / 10.0}℃"; extraInfo = "核心热量"; progress = temp / 10
        }
        hardwareItems[3].apply { value = "${volt}mV"; extraInfo = "电芯电压"; progress = volt / 50 }
        hardwareItems[4].apply {
            value = "${absCurrent}mA"; extraInfo = "充放电流"; progress = absCurrent / 10
        }
        hardwareItems[5].apply {
            value = "${power.toInt()}mW"; extraInfo = "功耗预估"; progress = power.toInt() / 50
        }

        // 通知搬运工：数据变了，赶紧重新刷一下屏幕
        adapter.notifyDataSetChanged()
    }

    // 定时任务处理器：每秒执行一次，负责更新计时器显示并刷新硬件数据，通过 postDelayed 实现自我调用形成循环
    private val timerRunnable = object : Runnable {
        override fun run() {
            secondsElapsed++
            val minutes = secondsElapsed / 60
            val seconds = secondsElapsed % 60
            tvTimer.text = "测试计时：${minutes}分${seconds}秒"
            refreshHardwareData()
            handler.postDelayed(this, 1000)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshHardwareData() // 听到系统大喊“电池变了”，立刻主动刷新一次
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 【数据接力】：如果屏幕旋转或切换深色模式，从“保险箱”取回之前存的秒数
        if (savedInstanceState != null) {
            secondsElapsed = savedInstanceState.getInt("saved_seconds")
        }

        setContentView(R.layout.activity_main)

        // 1. 机型识别：直接通过 Build 库读取手机硬件名 (一加 7 Pro 用户的专属彩蛋)
        val tvDeviceModel = findViewById<TextView>(R.id.tvDeviceModel)
        tvDeviceModel.text = "本机机型：${Build.MANUFACTURER} ${Build.MODEL}"

        tvTimer = findViewById(R.id.tvTimer)

        // 2. 初始化 6 个空的储物盒
        hardwareItems = mutableListOf(
            HardwareItem("电池电量", "0%", "...", 0),
            HardwareItem("系统内存", "0%", "...", 0),
            HardwareItem("电池温度", "0℃", "...", 0),
            HardwareItem("电池电压", "0mV", "...", 0),
            HardwareItem("实时电流", "0mA", "...", 0),
            HardwareItem("实时功耗", "0mW", "...", 0)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.hardwareList)
        // 使用网格布局，每行容纳 2 个监控单元
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        //创建一个 HardwareAdapter 适配器对象，把刚才初始化的 6 个硬件数据项传进去。
        adapter = HardwareAdapter(hardwareItems)
        //然后把适配器绑定到 RecyclerView 上，相当于告诉列表：'用这个适配器来渲染数据'。
        recyclerView.adapter = adapter
    }

    // 【存入保险箱】：当系统因为换皮肤要重启 Activity 时，先把重要的秒数存起来
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("saved_seconds", secondsElapsed)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        handler.post(timerRunnable) // 页面回到前台，时钟开始走动
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
        handler.removeCallbacks(timerRunnable) // 页面退到后台，立刻停掉计时器，避免偷跑流量和电量
    }
}