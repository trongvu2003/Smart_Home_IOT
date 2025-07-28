package com.example.smart_home_iot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        _errorMessage.value = task.exception?.message ?: "Đăng nhập thất bại"
                    }
                    _isLoading.value = false
                }
        }
    }

    fun signup(username: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserProfile(
                            uid = auth.currentUser!!.uid,
                            username = username,
                            email = email,
                            phone = phone
                        )
                    } else {
                        _errorMessage.value = task.exception?.message ?: "Đăng ký thất bại"
                        _isLoading.value = false
                    }
                }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user!!
                        firestore.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    saveUserProfile(
                                        uid = user.uid,
                                        username = user.displayName ?: "Người dùng Google",
                                        email = user.email!!,
                                        phone = user.phoneNumber ?: ""
                                    )
                                } else {
                                    _isLoading.value = false
                                }
                            }
                    } else {
                        _errorMessage.value = task.exception?.message ?: "Đăng nhập Google thất bại"
                        _isLoading.value = false
                    }
                }
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun saveUserProfile(uid: String, username: String, email: String, phone: String) {
        val userProfile = hashMapOf(
            "username" to username,
            "email" to email,
            "phone" to phone,
            "uid" to uid
        )
        firestore.collection("users").document(uid)
            .set(userProfile)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _errorMessage.value = task.exception?.message ?: "Lỗi khi lưu thông tin"
                }
                _isLoading.value = false
            }
    }
}