package com.example.keepup.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keepup.FirestoreRepository
import com.example.keepup.Workout
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun WorkoutDetailsScreen(
    workoutId: String,
    onBack: () -> Unit
) {
    val repository = FirestoreRepository()
    var workout by remember { mutableStateOf<Workout?>(null) }

    // Fetch workout details from Firestore
    LaunchedEffect(workoutId) {
        repository.fetchWorkoutDetails(
            workoutId,
            onSuccess = { fetchedWorkout ->
                workout = fetchedWorkout
            },
            onFailure = { exception ->
                Log.e("WorkoutDetailsScreen", "Failed to fetch workout: ${exception.message}")
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F2F2)
    ) {
        if (workout != null) {
            val validatedWorkout = workout!!
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    validatedWorkout.path.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                        ?: LatLng(0.0, 0.0),
                    15f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Section
                Text(
                    text = "Workout Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(text = "Date: ${validatedWorkout.date}", fontSize = 16.sp)
                        Text(text = "Type: ${validatedWorkout.type}", fontSize = 16.sp)
                        Text(text = "Duration: ${validatedWorkout.elapsedTime}", fontSize = 16.sp)
                        Text(text = "Distance: ${validatedWorkout.distance} km", fontSize = 16.sp)
                        Text(text = "Avg. Speed: ${validatedWorkout.avgSpeed} km/h", fontSize = 16.sp)
                        Text(text = "Points: ${validatedWorkout.points}", fontSize = 16.sp)
                    }
                }

                // Map Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .padding(vertical = 16.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        // Draw Polyline for the workout path
                        if (validatedWorkout.path.isNotEmpty()) {
                            Polyline(
                                points = validatedWorkout.path.map { LatLng(it.latitude, it.longitude) },
                                color = Color(0xFF6200EE),
                                width = 8f
                            )

                            // Add a marker at the start of the path
                            Marker(
                                state = rememberMarkerState(
                                    position = LatLng(
                                        validatedWorkout.path.first().latitude,
                                        validatedWorkout.path.first().longitude
                                    )
                                ),
                                title = "Start Point"
                            )
                        }
                    }
                }

                // Back Button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text(text = "Back", color = Color.White, fontSize = 16.sp)
                }
            }
        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6200EE))
            }
        }
    }
}
