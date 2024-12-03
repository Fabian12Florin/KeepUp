package com.example.keepup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ActivityHistoryItem(
    val date: String,
    val type: String,
    val duration: String,
    val distance: String,
    val calories: String
)

@Composable
fun HistoryScreen() {
    val historyItems = listOf(
        ActivityHistoryItem("25 May 2024", "Running", "30 min", "5 km", "300 kcal"),
        ActivityHistoryItem("24 May 2024", "Cycling", "45 min", "10 km", "450 kcal"),
        ActivityHistoryItem("23 May 2024", "Walking", "20 min", "2 km", "100 kcal"),
        // Adaugă mai multe activități după cum dorești
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Activity History",
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(historyItems) { item ->
                ActivityHistoryCard(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ActivityHistoryCard(item: ActivityHistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.date, style = MaterialTheme.typography.bodySmall)
            Text(text = item.type, fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
            Text(text = "Duration: ${item.duration}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Distance: ${item.distance}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Calories: ${item.calories}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
