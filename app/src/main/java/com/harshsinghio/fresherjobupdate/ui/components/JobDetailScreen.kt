package com.harshsinghio.fresherjobupdate.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.model.JobPosting
import java.text.SimpleDateFormat
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobPosting: JobPosting,
    onBackClick: () -> Unit,
    onStatusUpdate: (ApplicationStatus) -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onFavoriteToggle(!jobPosting.isFavorite) }) {
                        Icon(
                            if (jobPosting.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (jobPosting.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Source and time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${jobPosting.source}",
                    style = MaterialTheme.typography.bodyMedium
                )

                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(jobPosting.timestamp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = jobPosting.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content with clickable links
            val content = jobPosting.content
            ClickableLinksText(text = content)

            Spacer(modifier = Modifier.height(24.dp))

            // Application Status
            var showStatusDropdown by remember { mutableStateOf(false) }

            Column {
                Text(
                    text = "Application Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { showStatusDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(jobPosting.applicationStatus.name.replace("_", " "))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Status")
                    }

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        ApplicationStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name.replace("_", " ")) },
                                onClick = {
                                    onStatusUpdate(status)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Button to extract and call a phone number if found
                ExtendedFloatingActionButton(
                    onClick = {
                        extractPhoneNumber(jobPosting.content)?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        }
                    },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Call") },
                    text = { Text("Call") }
                )

                // Button to extract and open email if found
                ExtendedFloatingActionButton(
                    onClick = {
                        extractEmail(jobPosting.content)?.let { email ->
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, "Job Application: ${jobPosting.title}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    text = { Text("Email") }
                )
            }
        }
    }
}

@Composable
fun ClickableLinksText(text: String) {
    val context = LocalContext.current
    val linkPattern = Regex("""(https?://[^\s]+)|([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})|(\+?\d{10,})""")

    // Find all matches of URLs, emails, and phone numbers
    val matches = linkPattern.findAll(text).toList()

    if (matches.isEmpty()) {
        // If no links found, just display the text normally
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    // Build an AnnotatedString with clickable spans
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0

        matches.forEach { matchResult ->
            // Add text before this match
            val startIndex = matchResult.range.first
            if (startIndex > lastIndex) {
                append(text.substring(lastIndex, startIndex))
            }

            // Add the clickable link
            val linkText = matchResult.value
            val endIndex = matchResult.range.last + 1

            pushStringAnnotation(tag = "URL", annotation = linkText)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(linkText)
            }
            pop()

            lastIndex = endIndex
        }

        // Add any remaining text after the last link
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    // Use ClickableText with the annotated string
    ClickableText(
        text = annotatedString,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val url = annotation.item
                    val intent = when {
                        url.startsWith("http") -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        url.contains("@") -> Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$url")
                        }
                        else -> Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$url")
                        }
                    }
                    context.startActivity(intent)
                }
        }
    )
}

private fun extractPhoneNumber(text: String): String? {
    val phonePattern = Regex("""(\+?\d{10,})""")
    val match = phonePattern.find(text)
    return match?.value
}

private fun extractEmail(text: String): String? {
    val emailPattern = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
    val match = emailPattern.find(text)
    return match?.value
}