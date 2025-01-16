package com.example.keepup

import android.content.Context
import android.content.Intent

class utils {
}

// Funcție pentru formatarea timpului
fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

fun saveStepGoal(context: Context, stepGoal: Int) {
    val sharedPref = context.getSharedPreferences("KeepUpPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putInt("step_goal", stepGoal)
        apply()
    }
}
