# HardwarePulse - Android 硬件脉动监控

**HardwarePulse** 是一款专为 Android 打造的轻量级硬件实时监控工具。它通过直观的仪表盘界面，帮助用户实时掌握设备的电池健康、系统内存以及功耗状态。

## 🚀 项目亮点

* **实时性能监测**：每秒更新一次电池温度、电芯电压、实时电流及估算功率。
* **网格仪表盘布局**：采用 `GridLayoutManager` 实现 2x3 的响应式网格展示。
* **双模式自动适配**：完美适配深色模式（AMOLED 纯黑）与亮色模式，确保全天候清晰可见。
* **低开销架构**：利用 `Handler` 配合生命周期管理（`onResume/onPause`），在后台自动停止采集以节省电量。

## 🛠️ 技术栈

* **核心语言**：Kotlin (利用 `lateinit` 延迟初始化及 `apply` 作用域函数提升代码可读性)。
* **UI 框架**：`ConstraintLayout` + `RecyclerView` + 自定义环形进度条 (`layer-list` drawable)。
* **数据来源**：调用 Android 系统级服务 `BatteryManager` 与 `ActivityManager`。
* **内存计算公式**：

$$MB = \frac{TotalBytes - AvailableBytes}{1024 \times 1024}$$



## 📱 设备适配

* **首发测试平台**：OnePlus 7 Pro (GM1910)。
* **系统版本**：Android 10+。

## 📸 运行效果

| 深色模式 | 亮色模式 |
| --- | --- |
| <img width="463" height="1009" alt="image" src="https://github.com/user-attachments/assets/810252b7-113d-46ba-8839-e61f4768afd0" /> | <img width="463" height="1009" alt="image" src="https://github.com/user-attachments/assets/907aa689-9284-417d-b853-3576ea6b84fb" /> |
| 
 | 
 |
