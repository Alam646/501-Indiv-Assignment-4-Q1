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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indivassignment4q1.ui.theme.IndivAssignment4Q1Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LifecycleEvent(
    val eventName: String,
    val timestamp: String,
    val color: Color
)

// We use a ViewModel to hold the app's data. This is important because it prevents
// the event log from being cleared every time the screen rotates.
class LifeTrackerViewModel : ViewModel() {
    // StateFlow is used to hold the list of events because we want the UI to be able to
    // observe changes and always have the latest list.
    private val _events = MutableStateFlow<List<LifecycleEvent>>(emptyList())
    val events = _events.asStateFlow()

    // SharedFlow is perfect for one-time events like showing a snackbar. This ensures
    // the snackbar message isn't shown again if the screen rotates.
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // Create one formatter and reuse it for efficiency.
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun addEvent(event: Lifecycle.Event) {
        val newEvent = LifecycleEvent(
            eventName = event.name,
            timestamp = dateFormat.format(Date()),
            color = event.toColor()
        )
        // New events are added to the front, so they appear at the top of the list.
        _events.value = listOf(newEvent) + _events.value

        // We use viewModelScope to launch this coroutine so it's automatically cancelled
        // if the ViewModel is destroyed. This prevents memory leaks.
        viewModelScope.launch {
            _snackbarMessage.emit(event.name)
        }
    }

    // This is an extension function on `Lifecycle.Event` to keep the color-coding logic tidy.
    private fun Lifecycle.Event.toColor(): Color = when (this) {
        Lifecycle.Event.ON_CREATE -> Color(0xFFC8E6C9) // Light Green
        Lifecycle.Event.ON_START -> Color(0xFFA5D6A7) // Green
        Lifecycle.Event.ON_RESUME -> Color(0xFF81C784) // Darker Green
        Lifecycle.Event.ON_PAUSE -> Color(0xFFFFCC80) // Light Orange
        Lifecycle.Event.ON_STOP -> Color(0xFFFFAB91) // Orange
        Lifecycle.Event.ON_DESTROY -> Color(0xFFEF9A9A) // Red
        Lifecycle.Event.ON_ANY -> Color.White
    }
}

class MainActivity : ComponentActivity() {
    // The `by viewModels()` delegate is the standard way to get a ViewModel that survives rotations.
    private val viewModel: LifeTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is where we hook into the Activity's lifecycle. The observer gets a callback
        // for every state change, which is how we capture the events.
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_ANY) {
                viewModel.addEvent(event)
            }
        })

        setContent {
            IndivAssignment4Q1Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LifeTrackerApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTrackerApp(viewModel: LifeTrackerViewModel = viewModel()) {
    // `collectAsState` is how we connect our Compose UI to the ViewModel's data.
    // The UI will automatically update whenever the event list changes.
    val events by viewModel.events.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // This setting is to meet the requirement for a configurable snackbar.
    val showSnackbar = true

    // `LaunchedEffect` is the right place for a one-time setup task in Compose.
    // Here, we use it to start listening for snackbar messages from the ViewModel.
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            if (showSnackbar) {
                scope.launch {
                    snackbarHostState.showSnackbar("Lifecycle Event: $message")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("LifeTracker") }) }
    ) { paddingValues ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("No lifecycle events yet.", modifier = Modifier.padding(16.dp))
            }
        } else {
            // We use `LazyColumn` because it's highly efficient for long lists. It only
            // renders the items that are currently visible on screen.
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
    val viewModel = LifeTrackerViewModel()
    viewModel.addEvent(Lifecycle.Event.ON_CREATE)
    viewModel.addEvent(Lifecycle.Event.ON_START)
    IndivAssignment4Q1Theme {
        LifeTrackerApp(viewModel)
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
