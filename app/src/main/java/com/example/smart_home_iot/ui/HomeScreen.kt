import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_iot.ui.home.AddDeviceDialog
import com.example.smart_home_iot.ui.home.AddRoomDialog
import com.example.smart_home_iot.ui.home.RoomCard
import com.example.smart_home_iot.viewmodel.HomeViewModel
import com.example.smart_house.data.firebase.UserRepository
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel as viewModel1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel1()
) {
    val rooms by homeViewModel.rooms.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var currentRoomForAddingDevice by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var username by remember { mutableStateOf("Đang tải...") }
    LaunchedEffect(Unit) {
        username= UserRepository.getUserName()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart House", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {  }) { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddRoomDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm phòng")
            }
        }
    ) { innerPadding ->
        if (showAddRoomDialog) {
            AddRoomDialog(
                onDismiss = { showAddRoomDialog = false },
                onConfirm = { roomName ->
                    homeViewModel.addRoom(roomName) { success, error ->
                        if (success) {
                            Toast.makeText(context, "Đã thêm phòng '$roomName'", Toast.LENGTH_SHORT).show()
                            showAddRoomDialog = false
                        } else {
                            Toast.makeText(context, "Lỗi: ${error ?: "Không thể thêm phòng"}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        if (showAddDeviceDialog) {
            AddDeviceDialog(
                onDismiss = { showAddDeviceDialog = false },
                onConfirm = { deviceName, iconName ->
                    currentRoomForAddingDevice?.let { roomId ->
                        homeViewModel.addDeviceToRoom(roomId, deviceName, iconName) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Đã thêm thiết bị '$deviceName'", Toast.LENGTH_SHORT).show()
                                showAddDeviceDialog = false
                            } else {
                                Toast.makeText(context, "Lỗi: ${error ?: "Không thể thêm thiết bị"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Xin chào, $username!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Chào mừng bạn trở về nhà.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Text("Các phòng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                }

                if (rooms.isEmpty()) {
                    item {
                        Text("Bạn chưa có phòng nào. Hãy nhấn nút '+' để thêm phòng mới.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(rooms, key = { it.id }) { room ->
                        RoomCard(
                            room = room,
                            onUpdateDevice = { deviceId, newStatus -> homeViewModel.updateDeviceStatus(room.id, deviceId, newStatus) },
                            onAddDeviceClick = {
                                currentRoomForAddingDevice = room.id
                                showAddDeviceDialog = true
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

