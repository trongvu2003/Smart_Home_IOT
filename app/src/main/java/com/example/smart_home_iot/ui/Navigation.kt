package com.example.smart_home_iot.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {
        composable("splash") {
            SplashScreen(onOpenApp = { navController.navigate("login") { popUpTo("splash") { inclusive = true } } })
        }
        composable("login") {
            LoginScreen(
                onLogin = {},
                onSignup = { navController.navigate("signup") },
                onForgotPassword = {},
                onFacebookLogin = {},
                onGoogleLogin = {}
            )
        }
        composable("signup") {
            SignupScreen(
                onSignup = {},
                onLogin = { navController.popBackStack() },
                onFacebookLogin = {},
                onGoogleLogin = {}
            )
        }
    }
} 