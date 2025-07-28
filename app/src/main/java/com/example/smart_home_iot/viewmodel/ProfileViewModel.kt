package com.example.smart_home_iot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            _email.value = user?.email ?: ""
            val firebasePhone = user?.phoneNumber ?: ""
            if (firebasePhone.isNotBlank()) {
                _phone.value = firebasePhone
            } else {
                val emailKey = _email.value.lowercase()
                val prefs = getApplication<Application>().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                val localPhone = prefs.getString("phone_${emailKey}", "") ?: ""
                _phone.value = localPhone
            }
        }
    }

    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
        val emailKey = _email.value.lowercase()
        val prefs = getApplication<Application>().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("phone_${emailKey}", newPhone).apply()
    }

    fun onLogout(onResult: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onResult()
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (user != null && !email.isNullOrBlank()) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    onResult(true, null)
                                } else {
                                    onResult(false, updateTask.exception?.localizedMessage)
                                }
                            }
                    } else {
                        onResult(false, authTask.exception?.localizedMessage)
                    }
                }
        } else {
            onResult(false, "Không tìm thấy tài khoản.")
        }
    }
} 