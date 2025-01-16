package com.example.keepup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.keepup.FirestoreRepository

data class ActivityHistoryItem(
    val workoutId: String, // Adaugă acest câmp
    val date: String,
    val type: String,
    val duration: String,
    val distance: String,
    val calories: String
)

@Composable
fun HistoryScreen(navController: NavController) {
    // Background colors
    val backgroundColor = Color(0xFFF2F2F2)
    val primaryColor = Color(0xFF6200EE)

    // Mutable state for holding the history items fetched from Firestore
    val historyItems = remember { mutableStateListOf<ActivityHistoryItem>() }

    // Instantiate FirestoreRepository
    val firestoreRepository = FirestoreRepository()

    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        firestoreRepository.fetchWorkouts(
            onResult = { workouts ->
                historyItems.clear()
                historyItems.addAll(
                    workouts.map { workout ->
                        ActivityHistoryItem(
                            workoutId = workout.id, // ID-ul documentului
                            date = workout.date,
                            type = workout.type,
                            duration = workout.elapsedTime,
                            distance = "${workout.distance} km",
                            calories = "N/A" // sau calculează caloriile
                        )
                    }
                )
            },
            onFailure = { exception ->
                println("Error fetching workouts: ${exception.message}")
            }
        )
    }

    // UI pentru listă
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "History",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(historyItems) { item ->
                ActivityHistoryCard(item = item) {
                    // Navighează la `WorkoutDetailsScreen`
                    navController.navigate("workoutDetails/${item.workoutId}")
                }
            }
        }
    }
}


@Composable
fun ActivityHistoryCard(item: ActivityHistoryItem, onClick: () -> Unit) {
    val activityIcon = when (item.type.lowercase()) {
        "running" -> Icons.Default.DirectionsRun
        "cycling" -> Icons.Default.DirectionsBike
        "walking" -> Icons.Default.DirectionsWalk
        else -> Icons.Default.DirectionsRun
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }, // Handle click
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    activityIcon,
                    contentDescription = "${item.type} Icon",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.type,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Duration: ${item.duration}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Text(
                text = "Distance: ${item.distance}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Text(
                text = "Calories: ${item.calories}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}
