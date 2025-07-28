package com.example.smart_home_iot.ui

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smart_home_iot.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    onGoogleLogin: () -> Unit,
) {
    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
        composable("splash") {
            WelcomeScreen(onOpenApp = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onSignup = { navController.navigate("signup") },
                onGoogleLogin = onGoogleLogin
            )
        }
        composable("signup") {
            SignupScreen(
                authViewModel = authViewModel,
                onLogin = { navController.navigateUp() },
                onFacebookLogin = {},
                onGoogleLogin = onGoogleLogin
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = { authViewModel.logout() },
                navController = navController
            )
        }
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("notifications") {
            NotificationScreen(navController = navController)
        }
    }
    DisposableEffect(navController) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (auth.currentUser != null) {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            } else {
                if (currentRoute != "login" && currentRoute != "splash") {
                    navController.navigate("login") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }
}