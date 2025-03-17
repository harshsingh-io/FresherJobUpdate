package com.harshsinghio.fresherjobupdate

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.ui.components.*

class MainActivity : ComponentActivity() {

    private val viewModel: JobFinderViewModel by viewModels {
        JobFinderViewModelFactory((application as JobFinderApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if notification access is enabled
        checkNotificationAccess()

        setContent {
            JobFinderApp(viewModel = viewModel)
        }
    }

    private fun checkNotificationAccess() {
        val enabledNotificationListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )

        // If our service is not in the list of enabled listeners
        if (enabledNotificationListeners == null ||
            !enabledNotificationListeners.contains(packageName)) {

            // Show a dialog to request notification access
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)

            Toast.makeText(
                this,
                "Please enable notification access for this app",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFinderApp(viewModel: JobFinderViewModel) {
    // State collection
    val jobPostings by viewModel.filteredJobPostings.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentJobPosting by viewModel.currentJobPosting.collectAsStateWithLifecycle()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsStateWithLifecycle()
    val selectedJobPostings by viewModel.selectedJobPostings.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()

    // Local state
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var showStatusOptions by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fresher Job Update") },
                    actions = {
                        if (isMultiSelectMode) {
                            // Multi-select mode actions
                            IconButton(onClick = {
                                val selectedJobs = jobPostings.filter { selectedJobPostings.contains(it.id) }
                                val shareText = buildString {
                                    append("Shared Job Openings:\n\n")
                                    selectedJobs.forEachIndexed { index, job ->
                                        append("${index + 1}. ${job.title}\n")
                                        append("From: ${job.source}\n")
                                        append("${job.content}\n\n")
                                    }
                                }

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Selected"
                                )
                            }

                            IconButton(onClick = { viewModel.deleteSelectedJobPostings() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected"
                                )
                            }

                            IconButton(onClick = { showStatusOptions = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Set Status"
                                )
                            }
                        } else {
                            // Normal mode actions
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter"
                                )
                            }

                            IconButton(onClick = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        // Only show the close button in multi-select mode
                        if (isMultiSelectMode) {
                            IconButton(onClick = { viewModel.toggleMultiSelectMode() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Selection"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (isMultiSelectMode) {
                            viewModel.selectAll(jobPostings.map { it.id })
                        } else {
                            viewModel.toggleMultiSelectMode()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isMultiSelectMode) Icons.Default.SelectAll else Icons.Default.CheckBox,
                        contentDescription = if (isMultiSelectMode) "Select All" else "Multi-Select"
                    )
                }
            }
        ) { paddingValues ->
            // Main content area
            Box(modifier = Modifier.padding(paddingValues)) {
                // Show either the detail view or the list view
                if (currentJobPosting != null) {
                    // Detail view
                    JobDetailScreen(
                        jobPosting = currentJobPosting!!,
                        onBackClick = { viewModel.clearCurrentJobPosting() },
                        onStatusUpdate = { status ->
                            viewModel.updateApplicationStatus(currentJobPosting!!.id, status)
                        },
                        onFavoriteToggle = { isFavorite ->
                            viewModel.toggleFavorite(currentJobPosting!!.id, isFavorite)
                        },
                        onShare = {
                            val shareText = "Job: ${currentJobPosting!!.title}\n\n${currentJobPosting!!.content}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        },
                        onDelete = {
                            viewModel.delete(currentJobPosting!!)
                            viewModel.clearCurrentJobPosting()
                        }
                    )
                } else if (jobPostings.isEmpty()) {
                    // Empty state
                    EmptyState(PaddingValues())
                } else {
                    // List view
                    JobPostingList(
                        jobPostings = jobPostings,
                        isMultiSelectMode = isMultiSelectMode,
                        selectedIds = selectedJobPostings,
                        onJobPostingClicked = { jobPosting ->
                            viewModel.setCurrentJobPosting(jobPosting)
                        },
                        onJobPostingLongClicked = {
                            if (!isMultiSelectMode) {
                                viewModel.toggleMultiSelectMode()
                                viewModel.toggleSelection(it.id)
                            }
                        },
                        onDeleteClicked = { viewModel.delete(it) },
                        onFavoriteToggle = { jobPosting, isFavorite ->
                            viewModel.toggleFavorite(jobPosting.id, isFavorite)
                        },
                        onSelectionToggle = { id ->
                            viewModel.toggleSelection(id)
                        }
                    )
                }
            }

            // Modals and dialogs
            if (showFilterSheet) {
                ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
                    FilterBottomSheet(
                        currentFilter = currentFilter,
                        onFilterSelected = { filter ->
                            viewModel.resetFilter()
                            when (filter) {
                                FilterType.FAVORITES -> viewModel.showFavorites()
                                else -> viewModel.resetFilter()
                            }
                            showFilterSheet = false
                        },
                        onSourceFilterSelected = { source ->
                            viewModel.setFilterBySource(source)
                            showFilterSheet = false
                        },
                        onDateFilterSelected = { start, end ->
                            viewModel.setFilterByDateRange(start, end)
                            showFilterSheet = false
                        },
                        onStatusFilterSelected = { status ->
                            viewModel.setFilterByStatus(status)
                            showFilterSheet = false
                        },
                        onDismiss = { showFilterSheet = false }
                    )
                }
            }

            if (showStatusOptions) {
                AlertDialog(
                    onDismissRequest = { showStatusOptions = false },
                    title = { Text("Update Status") },
                    text = {
                        Column {
                            ApplicationStatus.values().forEach { status ->
                                Button(
                                    onClick = {
                                        viewModel.updateSelectedApplicationStatus(status)
                                        showStatusOptions = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(status.name.replace("_", " "))
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showStatusOptions = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}