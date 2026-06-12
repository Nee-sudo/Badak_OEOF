package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.OeofRepository
import com.example.ui.MainViewModel
import com.example.ui.OeofMainRouter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room SQLite Database & Repository Instantiation
        val database = AppDatabase.getDatabase(this)
        val repository = OeofRepository(
            notificationDao = database.notificationDao(),
            userStatsDao = database.userStatsDao()
        )

        // Lifecycle ViewModel Provider utilizing Constructor Injection
        val viewModel: MainViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        setContent {
            OeofMainRouter(viewModel = viewModel)
        }
    }
}
