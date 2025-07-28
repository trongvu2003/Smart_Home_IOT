package com.example.smart_home_iot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_home_iot.R
import com.example.smart_home_iot.viewmodel.ProfileViewModel
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val context = LocalContext.current
    var showChangePassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var newPhone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName
        ?: user?.email?.substringBefore("@")
        ?: "Người dùng"

    val emailKey = email?.lowercase() ?: "unknown"
    val avatarFileName = "avatar_${emailKey}.jpg"
    val avatarFile = File(context.filesDir, avatarFileName)
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }
    var avatarPath by remember {
        mutableStateOf(
            prefs.getString("avatar_path_${emailKey}", null)
        )
    }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(avatarFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                avatarPath = avatarFile.absolutePath
                prefs.edit().putString("avatar_path_${emailKey}", avatarFile.absolutePath).apply()
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể lưu ảnh đại diện", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            onChangePassword = { oldPass, newPass ->
                isLoading = true
                viewModel.changePassword(oldPass, newPass) { success, error ->
                    isLoading = false
                    showChangePassword = false
                    Toast.makeText(
                        context,
                        if (success) "Đổi mật khẩu thành công!" else (error ?: "Đổi mật khẩu thất bại!"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onDismiss = { showChangePassword = false }
        )
    }

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false; phoneError = null },
            confirmButton = {
                Button(onClick = {
                    val regex = Regex("^0[0-9]{9,10}")
                    if (!regex.matches(newPhone)) {
                        phoneError = "Số điện thoại không hợp lệ!"
                    } else {
                        viewModel.updatePhone(newPhone)
                        showPhoneDialog = false
                        phoneError = null
                    }
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false; phoneError = null }) { Text("Hủy") }
            },
            title = { Text("Thêm số điện thoại") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it; phoneError = null },
                        label = { Text("Nhập số điện thoại mới") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneError != null
                    )
                    if (phoneError != null) {
                        Text(
                            phoneError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Thông tin cá nhân",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0))
                    .clickable { pickImageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(avatarPath) {
                    try {
                        avatarPath?.let { path ->
                            val file = File(path)
                            if (file.exists()) {
                                android.graphics.BitmapFactory.decodeFile(path)
                            } else null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Avatar",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Email", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(email.ifBlank { "Chưa có email" }, color = Color.Gray, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .let {
                        if (phone.isBlank()) {
                            it.then(
                                Modifier.clickable(onClick = { showPhoneDialog = true })
                            )
                        } else it
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Số điện thoại", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            if (phone.isBlank()) "Chưa cập nhật" else phone,
                            color = Color(0xFFFF9800), fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Đổi mật khẩu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { showChangePassword = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onLogout(onLogout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}