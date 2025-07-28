package com.example.smart_home_iot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.smart_home_iot.model.Device
import com.example.smart_home_iot.model.Room
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms = _rooms.asStateFlow()

    init {
        listenForUserData()
    }

    private fun listenForUserData() {
        val userId = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return
        }
        _isLoading.value = true
        firestore.collection("users").document(userId).collection("rooms")
            .addSnapshotListener { roomSnapshot, error ->
                if (error != null) {
                    Log.w("HomeViewModel", "Listen to rooms failed.", error)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val roomList = roomSnapshot?.documents?.mapNotNull {
                    it.toObject<Room>()?.copy(id = it.id)
                } ?: emptyList()

                if (roomList.isEmpty()) {
                    _rooms.value = emptyList()
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                roomList.forEach { room ->
                    listenToRoomDevices(userId, room)
                }
            }
    }

    private fun listenToRoomDevices(userId: String, room: Room) {
        firestore.collection("users").document(userId).collection("rooms").document(room.id)
            .collection("devices")
            .addSnapshotListener { deviceSnapshot, error ->
                if (error != null) {
                    Log.w("HomeViewModel", "Listen to devices in ${room.name} failed.", error)
                    return@addSnapshotListener
                }

                room.devices = deviceSnapshot?.documents?.mapNotNull {
                    it.toObject<Device>()?.copy(id = it.id)
                } ?: emptyList()

                updateRoomsState(room)
            }
    }

    private fun updateRoomsState(updatedRoom: Room) {
        val currentRooms = _rooms.value.toMutableList()
        val index = currentRooms.indexOfFirst { it.id == updatedRoom.id }
        if (index != -1) {
            currentRooms[index] = updatedRoom
        } else {
            currentRooms.add(updatedRoom)
        }
        _rooms.value = currentRooms.sortedBy { it.name }
        _isLoading.value = false
    }

    fun updateDeviceStatus(roomId: String, deviceId: String, newStatus: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val originalRooms = _rooms.value
        val newRooms = originalRooms.map { room ->
            if (room.id == roomId) {
                val newDevices = room.devices.map { device ->
                    if (device.id == deviceId) {
                        device.copy(status = newStatus)
                    } else {
                        device
                    }
                }
                room.copy(devices = newDevices)
            } else {
                room
            }
        }
        _rooms.value = newRooms
        firestore.collection("users").document(userId)
            .collection("rooms").document(roomId)
            .collection("devices").document(deviceId)
            .update("status", newStatus)
            .addOnFailureListener { e ->
                Log.w("HomeViewModel", "Lỗi khi cập nhật thiết bị, đang hoàn tác UI.", e)
                _rooms.value = originalRooms
            }
    }

    fun addRoom(roomName: String, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return onResult(false, "Người dùng chưa đăng nhập.")
        }
        if (roomName.isBlank()) {
            _isLoading.value = false
            return onResult(false, "Tên phòng không được để trống.")
        }

        firestore.collection("users").document(userId).collection("rooms")
            .add(hashMapOf("name" to roomName))
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onResult(false, e.message)
            }
    }

    fun addDeviceToRoom(roomId: String, deviceName: String, iconName: String, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return onResult(false, "Người dùng chưa đăng nhập.")
        }
        if (deviceName.isBlank()) {
            _isLoading.value = false
            return onResult(false, "Tên thiết bị không được để trống.")
        }

        val newDevice = Device(name = deviceName, iconName = iconName, status = false)

        firestore.collection("users").document(userId)
            .collection("rooms").document(roomId)
            .collection("devices").add(newDevice)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onResult(false, e.message)
            }
    }
}