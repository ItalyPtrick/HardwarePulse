package com.example.hardwarepulse

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HardwareAdapter(private val data: List<HardwareItem>) :
    RecyclerView.Adapter<HardwareAdapter.HardwareViewHolder>() {

    // 【管家】：负责记住房间里所有家电（View）的位置，避免每次刷新都要去重新找
    class HardwareViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvHardwareName)
        val value: TextView = view.findViewById(R.id.tvHardwareValue)
        val extra: TextView = view.findViewById(R.id.tvHardwareExtra)
        val progress: ProgressBar = view.findViewById(R.id.pbCircle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HardwareViewHolder {
        // 把 XML 施工图纸吹气膨胀成一个真实的、可以住人的“房间”
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hardware, parent, false)
        return HardwareViewHolder(view)
    }

    override fun onBindViewHolder(holder: HardwareViewHolder, position: Int) {
        val item = data[position]

        // 按照图纸，把盒子里的东西一件件摆在桌面上
        holder.title.text = item.name
        holder.value.text = item.value
        holder.extra.text = item.extraInfo
        holder.progress.progress = item.progress

        // 交互功能：点击监控项时，触发物理震动反馈
        holder.itemView.setOnClickListener {
            val vibrator = it.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 震动 50 毫秒，强度为 64
                vibrator?.vibrate(VibrationEffect.createOneShot(50, 64))
            }
        }
    }

    override fun getItemCount(): Int = data.size
}