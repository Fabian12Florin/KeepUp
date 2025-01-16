package com.example.keepup.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.keepup.AuthScreen
import com.example.keepup.CyclingDetailsScreen
import com.example.keepup.RunningDetailsScreen
import com.example.keepup.WalkingDetailsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable(route = "auth") {
            Log.d("AppNavigation", "Navigating to: auth")
            AuthScreen(navController)
        }
        composable(route = "home") {
            Log.d("AppNavigation", "Navigating to: home")
            HomeScreen(navController)
        }
        composable(route = "activity") {
            Log.d("AppNavigation", "Navigating to: activity")
            ActivityScreen(navController)
        }
        composable(route = "history") {
            Log.d("AppNavigation", "Navigating to: history")
            HistoryScreen(navController)
        }
        composable(route = "runningDetails") {
            Log.d("AppNavigation", "Navigating to: runningDetails")
            RunningDetailsScreen(navController)
        }
        composable(route = "cyclingDetails") {
            Log.d("AppNavigation", "Navigating to: cyclingDetails")
            CyclingDetailsScreen(navController)
        }
        composable(route = "walkingDetails") {
            Log.d("AppNavigation", "Navigating to: walkingDetails")
            WalkingDetailsScreen(navController)
        }
        composable(
            route = "workoutDetails/{workoutId}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")
            if (workoutId != null) {
                Log.d("AppNavigation", "Navigating to: workoutDetails with ID: $workoutId")
                WorkoutDetailsScreen(
                    workoutId = workoutId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Log.e("AppNavigation", "workoutId is null")
            }
        }
    }
}
