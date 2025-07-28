package com.example.smart_home_iot.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.lifecycle.ViewModel
import com.example.smart_home_iot.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        loadSampleNotifications()
    }

    private fun loadSampleNotifications() {
        _notifications.value = listOf(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Phát hiện chuyển động ở cửa trước.",
                timestamp = "5 phút trước",
                icon = Icons.Default.Warning
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Đèn phòng khách đã được bật.",
                timestamp = "12 phút trước",
                icon = Icons.Default.Power
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Hệ thống đã được cập nhật.",
                timestamp = "1 giờ trước",
                icon = Icons.Default.Info
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Điều hòa phòng ngủ đã tắt.",
                timestamp = "3 giờ trước",
                icon = Icons.Default.Power
            )
        )
    }
}