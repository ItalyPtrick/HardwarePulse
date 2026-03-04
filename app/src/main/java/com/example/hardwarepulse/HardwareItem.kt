package com.example.hardwarepulse

// 核心数据模型：定义了每个监控项包含哪四种信息
data class HardwareItem(
    val name: String,     // 1. 标题文字（如“系统内存”）
    var value: String,    // 2. 圆圈中间的文字（如“67%”）
    var extraInfo: String, // 3. 圆圈底部的文字（如“4096MB/8192MB”）
    var progress: Int      // 4. 进度条的长度（0-100 的整数）
)