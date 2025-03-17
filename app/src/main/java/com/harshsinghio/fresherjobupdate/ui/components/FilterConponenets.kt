package com.harshsinghio.fresherjobupdate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.harshsinghio.fresherjobupdate.FilterType
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    onSourceFilterSelected: (String) -> Unit,
    onDateFilterSelected: (Date, Date) -> Unit,
    onStatusFilterSelected: (ApplicationStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(currentFilter) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filter Jobs",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter options
        Column(
            modifier = Modifier
                .selectableGroup()
                .fillMaxWidth()
        ) {
            FilterOption(
                icon = Icons.Default.List,
                title = "All Jobs",
                selected = selectedFilter == FilterType.ALL,
                onClick = {
                    selectedFilter = FilterType.ALL
                    onFilterSelected(FilterType.ALL)
                }
            )

            FilterOption(
                icon = Icons.Default.Apps,
                title = "By Source",
                selected = selectedFilter == FilterType.BY_SOURCE,
                onClick = { selectedFilter = FilterType.BY_SOURCE }
            )

            FilterOption(
                icon = Icons.Default.DateRange,
                title = "By Date",
                selected = selectedFilter == FilterType.BY_DATE,
                onClick = { selectedFilter = FilterType.BY_DATE }
            )

            FilterOption(
                icon = Icons.Default.CheckCircle,
                title = "By Status",
                selected = selectedFilter == FilterType.BY_STATUS,
                onClick = { selectedFilter = FilterType.BY_STATUS }
            )

            FilterOption(
                icon = Icons.Default.Favorite,
                title = "Favorites",
                selected = selectedFilter == FilterType.FAVORITES,
                onClick = {
                    selectedFilter = FilterType.FAVORITES
                    onFilterSelected(FilterType.FAVORITES)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show specific filter options based on selection
        when (selectedFilter) {
            FilterType.BY_SOURCE -> SourceFilterOptions(onSourceSelected = { source ->
                onSourceFilterSelected(source)
                onDismiss()
            })

            FilterType.BY_DATE -> DateFilterOptions(onDateRangeSelected = { start, end ->
                onDateFilterSelected(start, end)
                onDismiss()
            })

            FilterType.BY_STATUS -> StatusFilterOptions(onStatusSelected = { status ->
                onStatusFilterSelected(status)
                onDismiss()
            })

            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onFilterSelected(FilterType.ALL)
                    onDismiss()
                }
            ) {
                Text("Reset Filters")
            }
        }
    }
}

@Composable
fun FilterOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SourceFilterOptions(onSourceSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Source",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSourceSelected("WhatsApp") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Whatsapp,
                contentDescription = "WhatsApp"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("WhatsApp")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSourceSelected("Telegram") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Telegram"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Telegram")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterOptions(onDateRangeSelected: (Date, Date) -> Unit) {
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Date Range",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Start date
        OutlinedButton(
            onClick = { showStartDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Start Date"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("From: ${dateFormat.format(startDate)}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // End date
        OutlinedButton(
            onClick = { showEndDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "End Date"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("To: ${dateFormat.format(endDate)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onDateRangeSelected(startDate, endDate) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Date Filter")
        }

        // Date pickers
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Date(it)
                        }
                        showStartDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Date(it)
                        }
                        showEndDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun StatusFilterOptions(onStatusSelected: (ApplicationStatus) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Select Application Status",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        ApplicationStatus.values().forEach { status ->
            Button(
                onClick = { onStatusSelected(status) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(status.name.replace("_", " "))
            }
        }
    }
}