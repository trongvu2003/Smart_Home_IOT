package com.example.smart_home_iot.model

data class Device(
    val id: String = "",
    val name: String = "",
    val iconName: String = "",
    val status: Boolean = false
)