package com.example.keepup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.keepup.R // Replace with your actual R import

@Composable
fun ActivityScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F2F2) // Light background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Text(
                text = "Choose Your Activity",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6200EE)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Illustration
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp

            Image(
                painter = painterResource(id = R.drawable.activity_logo), // Replace with your illustration
                contentDescription = "Activities Illustration",
                modifier = Modifier
                    .height(screenHeight * 0.4f) // 40% of the screen height
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons for Activities
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActivityButton(
                    title = "Running",
                    icon = Icons.Default.DirectionsRun,
                    backgroundColor = Color(0xFFFF7043),
                    onClick = { navController.navigate("runningDetails") }
                )

                ActivityButton(
                    title = "Cycling",
                    icon = Icons.Default.DirectionsBike,
                    backgroundColor = Color(0xFF29B6F6),
                    onClick = { navController.navigate("cyclingDetails") }
                )

                ActivityButton(
                    title = "Walking",
                    icon = Icons.Default.DirectionsWalk,
                    backgroundColor = Color(0xFF66BB6A),
                    onClick = { navController.navigate("walkingDetails") }
                )

                ActivityButton(
                    title = "Yoga",
                    icon = Icons.Default.SelfImprovement,
                    backgroundColor = Color(0xFFAB47BC),
                    onClick = { navController.navigate("yogaDetails") }
                )
            }
        }
    }
}

@Composable
fun ActivityButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = "$title Icon",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
