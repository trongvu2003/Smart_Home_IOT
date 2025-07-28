package com.example.smart_home_iot.model

import androidx.compose.ui.graphics.vector.ImageVector

data class NotificationItem(
    val id: String,
    val title: String,
    val timestamp: String,
    val icon: ImageVector
)