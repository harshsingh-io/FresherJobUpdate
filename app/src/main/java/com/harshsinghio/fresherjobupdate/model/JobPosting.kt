package com.harshsinghio.fresherjobupdate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ApplicationStatus {
    NOT_APPLIED,
    APPLIED,
    REJECTED,
    NOT_APPLICABLE,
    INTERVIEW_SCHEDULED,
    OFFER_RECEIVED
}

@Entity(tableName = "job_postings")
data class JobPosting(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,                     // Which app (WhatsApp, Telegram)
    val title: String,                      // Notification title
    val content: String,                    // Notification content
    val timestamp: Long,                    // When notification was received
    val isRead: Boolean = false,            // Track if user has viewed this posting
    val applicationStatus: ApplicationStatus = ApplicationStatus.NOT_APPLIED,
    val isFavorite: Boolean = false         // For bookmarking important jobs
)