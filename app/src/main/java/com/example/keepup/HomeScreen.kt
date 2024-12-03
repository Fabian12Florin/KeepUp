package com.example.keepup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(userPoints: Int = 100) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Secțiune Bun venit
        Text(
            text = "Welcome back!",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Punctele utilizatorului
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You have",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$userPoints Points",
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informații despre activitatea zilei
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Today's Activity",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Duration: 30:48 min",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Calories: 745 kcal",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Avg. Speed: 16 km/h",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Avg. Heart Rate: 123 bpm",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
