package com.example.smart_home_iot.model

import com.google.firebase.firestore.Exclude
data class Room(
    val id: String = "",
    val name: String = "",
    @get:Exclude
    var devices: List<Device> = emptyList()
)
