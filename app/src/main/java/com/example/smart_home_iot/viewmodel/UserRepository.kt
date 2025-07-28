package com.example.smart_house.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserName(): String {
        val uid = auth.currentUser?.uid ?: return "Người dùng"
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.getString("username") ?: "Người dùng"
    }
}
