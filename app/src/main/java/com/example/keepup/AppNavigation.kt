package com.example.keepup.ui

import RunningDetailsScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.keepup.AuthScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") { AuthScreen(navController) }
        composable("home") { HomeScreen() }
        composable("activity") { ActivityScreen(navController) }
        composable("history") { HistoryScreen() }
        composable("runningDetails") { RunningDetailsScreen() }
    }
}
