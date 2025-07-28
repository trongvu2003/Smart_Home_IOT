package com.example.smart_home_iot.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.smart_home_iot.model.Device
import com.example.smart_home_iot.model.Room

val availableIcons = mapOf(
    "Bóng đèn" to "Lightbulb",
    "TV" to "Tv",
    "Máy lạnh" to "Thermostat"
)

@Composable
fun getIconFromName(iconName: String): ImageVector {
    return when (iconName) {
        "Lightbulb" -> Icons.Default.Lightbulb
        "Tv" -> Icons.Default.Tv
        "Thermostat" -> Icons.Default.Thermostat
        else -> Icons.Default.Lightbulb
    }
}

@Composable
fun RoomCard(
    room: Room,
    onUpdateDevice: (String, Boolean) -> Unit,
    onAddDeviceClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = room.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onAddDeviceClick) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm thiết bị vào ${room.name}")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (room.devices.isEmpty()) {
                Text("Chưa có thiết bị nào. Nhấn '+' để thêm.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val deviceChunks = room.devices.chunked(2)
                for (chunk in deviceChunks) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (device in chunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                DeviceControl(device = device, onStatusChange = { onUpdateDevice(device.id, it) })
                            }
                        }
                        if (chunk.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DeviceControl(device: Device, onStatusChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (device.status) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).height(100.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(imageVector = getIconFromName(device.iconName), contentDescription = device.name, modifier = Modifier.size(24.dp))
                Switch(checked = device.status, onCheckedChange = onStatusChange)
            }
            Column {
                Text(text = device.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = if (device.status) "Đang bật" else "Đã tắt", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddRoomDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var roomName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Thêm phòng mới", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = roomName, onValueChange = { roomName = it }, label = { Text("Tên phòng") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(roomName) }, enabled = roomName.isNotBlank()) { Text("Thêm") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (deviceName: String, iconName: String) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedIconKey by remember { mutableStateOf(availableIcons.keys.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Thêm thiết bị mới", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = deviceName, onValueChange = { deviceName = it }, label = { Text("Tên gọi cho thiết bị") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedIconKey,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại thiết bị") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableIcons.keys.forEach { key ->
                            DropdownMenuItem(text = { Text(key) }, onClick = {
                                selectedIconKey = key
                                expanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(deviceName, availableIcons[selectedIconKey]!!) }, enabled = deviceName.isNotBlank()) { Text("Thêm") }
                }
            }
        }
    }
}