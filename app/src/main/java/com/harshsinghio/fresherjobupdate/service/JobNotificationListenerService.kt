package com.harshsinghio.fresherjobupdate.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.harshsinghio.fresherjobupdate.JobFinderApplication
import com.harshsinghio.fresherjobupdate.model.JobPosting
import com.harshsinghio.fresherjobupdate.repository.JobPostingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class JobNotificationListenerService : NotificationListenerService() {

    private val TAG = "JobNotificationListener"
    private lateinit var jobRepository: JobPostingRepository

    // Specify the apps you want to monitor
    private val TARGET_APPS = listOf(
        "com.whatsapp",
        "org.telegram.messenger"
    )

    // Job posting patterns
    private val JOB_KEYWORDS = listOf(
        "job", "career", "hiring", "opening", "position", "vacancy",
        "recruitment", "opportunity", "apply", "fresher", "graduate"
    )

    // Pattern for 2025 batch or no year specified
    private val BATCH_PATTERN = Pattern.compile(
        ".*\\b2025\\s*batch\\b.*|" +  // Matches "2025 batch"
                ".*\\bfresher\\b.*|" +        // Matches "fresher"
                ".*\\brecent\\s*graduate\\b.*" // Matches "recent graduate"
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Notification listener service created")

        // Handle the application casting more safely
        if (application is JobFinderApplication) {
            jobRepository = (application as JobFinderApplication).repository
        } else {
            Log.e(TAG, "Application is not JobFinderApplication: ${application.javaClass.name}")
            // Create a workaround to avoid crashing
            val db = com.harshsinghio.fresherjobupdate.database.JobDatabase.getDatabase(applicationContext)
            jobRepository = com.harshsinghio.fresherjobupdate.repository.JobPostingRepository(db.jobDao())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Check if notification is from target apps
        if (!TARGET_APPS.contains(sbn.packageName)) return

        // Extract notification text
        val notification = sbn.notification
        val extras = notification.extras

        // Get title and text from notification
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Process in background
        CoroutineScope(Dispatchers.IO).launch {
            processNotification(sbn.packageName, title, text)
        }
    }

    private suspend fun processNotification(packageName: String, title: String, content: String) {
        val combinedText = "$title $content".lowercase()

        // Check if the notification contains job-related keywords
        val containsJobKeywords = JOB_KEYWORDS.any { combinedText.contains(it.lowercase()) }

        // Check if it matches our batch pattern (2025 batch or no year mentioned)
        val matchesBatchCriteria = BATCH_PATTERN.matcher(combinedText).matches() ||
                !Pattern.compile(".*\\b20[0-9]{2}\\s*batch\\b.*").matcher(combinedText).matches()

        if (containsJobKeywords && matchesBatchCriteria) {
            // Get app name from package
            val appName = when (packageName) {
                "com.whatsapp" -> "WhatsApp"
                "org.telegram.messenger" -> "Telegram"
                else -> packageName
            }

            // Save the job posting
            val jobPosting = JobPosting(
                id = 0, // Room will auto-generate
                source = appName,
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Save to database
            jobRepository.insert(jobPosting)

            Log.d(TAG, "Saved job posting: $title")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }
}