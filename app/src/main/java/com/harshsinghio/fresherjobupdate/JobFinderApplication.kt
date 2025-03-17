package com.harshsinghio.fresherjobupdate

import android.app.Application
import com.harshsinghio.fresherjobupdate.database.JobDatabase
import com.harshsinghio.fresherjobupdate.repository.JobPostingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class JobFinderApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    // Lazy initialization of database and repository
    private val database by lazy { JobDatabase.getDatabase(this) }
    val repository by lazy { JobPostingRepository(database.jobDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}