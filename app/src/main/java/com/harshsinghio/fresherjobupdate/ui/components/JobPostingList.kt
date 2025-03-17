package com.harshsinghio.fresherjobupdate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.model.JobPosting
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EmptyState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No job postings found yet",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Job notifications from WhatsApp and Telegram will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun JobPostingList(
    jobPostings: List<JobPosting>,
    isMultiSelectMode: Boolean,
    selectedIds: Set<Long>,
    onJobPostingClicked: (JobPosting) -> Unit,
    onJobPostingLongClicked: (JobPosting) -> Unit,
    onDeleteClicked: (JobPosting) -> Unit,
    onFavoriteToggle: (JobPosting, Boolean) -> Unit,
    onSelectionToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = jobPostings,
            key = { it.id }
        ) { jobPosting ->
            JobPostingItem(
                jobPosting = jobPosting,
                isMultiSelectMode = isMultiSelectMode,
                isSelected = selectedIds.contains(jobPosting.id),
                onClick = {
                    if (isMultiSelectMode) {
                        onSelectionToggle(jobPosting.id)
                    } else {
                        onJobPostingClicked(jobPosting)
                    }
                },
                onLongClick = { onJobPostingLongClicked(jobPosting) },
                onDeleteClick = { onDeleteClicked(jobPosting) },
                onFavoriteToggle = { isFavorite -> onFavoriteToggle(jobPosting, isFavorite) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostingItem(
    jobPosting: JobPosting,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(jobPosting.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source indicator and date
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Add app icon for the source
                            val sourceIcon = when (jobPosting.source) {
                                "WhatsApp" -> Icons.Default.Whatsapp
                                "Telegram" -> Icons.Default.Send
                                else -> Icons.Default.Notifications
                            }

                            Icon(
                                imageVector = sourceIcon,
                                contentDescription = jobPosting.source,
                                tint = when (jobPosting.source) {
                                    "WhatsApp" -> Color(0xFF25D366)
                                    "Telegram" -> Color(0xFF0088CC)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = jobPosting.source,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (!jobPosting.isRead) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Actions
                    Row {
                        // Multi-select checkbox
                        AnimatedVisibility(
                            visible = isMultiSelectMode,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onClick() }
                            )
                        }

                        // Favorite icon
                        IconButton(onClick = { onFavoriteToggle(!jobPosting.isFavorite) }) {
                            Icon(
                                imageVector = if (jobPosting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (jobPosting.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Delete icon if not in multi-select mode
                        AnimatedVisibility(
                            visible = !isMultiSelectMode,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = jobPosting.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Content
                Text(
                    text = jobPosting.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ApplicationStatusChip(status = jobPosting.applicationStatus)

                    if (jobPosting.isRead) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Read",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Read",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationStatusChip(status: ApplicationStatus) {
    val (backgroundColor, contentColor) = when (status) {
        ApplicationStatus.NOT_APPLIED -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        ApplicationStatus.APPLIED -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
        ApplicationStatus.INTERVIEW_SCHEDULED -> Pair(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiary)
        ApplicationStatus.OFFER_RECEIVED -> Pair(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), MaterialTheme.colorScheme.secondary)
        ApplicationStatus.REJECTED -> Pair(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), MaterialTheme.colorScheme.error)
        ApplicationStatus.NOT_APPLICABLE -> Pair(Color.LightGray, Color.DarkGray)
    }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}