package com.example.smart_home_iot.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onGoogleLogin: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
        composable("splash") {
            val auth = FirebaseAuth.getInstance()
            val onOpenApp: () -> Unit = {
                val destination = if (auth.currentUser != null) "home" else "login"
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            }
            WelcomeScreen(onOpenApp = onOpenApp)
        }
        composable("login") {
            LoginScreen(
                onLogin = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoogleLogin = onGoogleLogin,
                onSignup = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignupScreen(
                onSignup = {},
                onLogin = { navController.popBackStack() },
                onFacebookLogin = {},
                onGoogleLogin = onGoogleLogin
            )
        }
        composable("home") {
            HomeScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
            })
        }
    }
    DisposableEffect(navController) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            val currentRoute = navController.currentBackStackEntry?.destination?.route

            if (currentUser != null) {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            } else {
                if (currentRoute != "login") {
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }
}