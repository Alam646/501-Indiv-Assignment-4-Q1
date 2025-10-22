package com.example.indivassignment4q1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indivassignment4q1.ui.theme.IndivAssignment4Q1Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class to hold lifecycle event information
data class LifecycleEvent(
    val eventName: String,
    val timestamp: String,
    val color: Color
)

// ViewModel to store and manage lifecycle events
class LifeTrackerViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<LifecycleEvent>>(emptyList())
    val events = _events.asStateFlow()
}

class MainActivity : ComponentActivity() {
    private val viewModel: LifeTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndivAssignment4Q1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LifeTrackerApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTrackerApp(viewModel: LifeTrackerViewModel = viewModel()) {
    val events by viewModel.events.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("LifeTracker") })
        }
    ) { paddingValues ->
        if (events.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                Text("No lifecycle events yet.", modifier = Modifier.padding(16.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(events) { event ->
                    EventListItem(event)
                }
            }
        }
    }
}

@Composable
fun EventListItem(event: LifecycleEvent) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(event.color)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Event: ${event.eventName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Time: ${event.timestamp}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LifeTrackerAppPreview() {
    IndivAssignment4Q1Theme {
        LifeTrackerApp()
    }
}

@Preview(showBackground = true)
@Composable
fun EventListItemPreview() {
    val event = LifecycleEvent("ON_CREATE", "12:00:00.000", Color(0xFFC8E6C9))
    IndivAssignment4Q1Theme {
        EventListItem(event)
    }
}
