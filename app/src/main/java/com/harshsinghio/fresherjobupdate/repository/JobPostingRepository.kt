package com.harshsinghio.fresherjobupdate.repository

import androidx.annotation.WorkerThread
import com.harshsinghio.fresherjobupdate.dao.JobDao
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.model.JobPosting
import kotlinx.coroutines.flow.Flow
import java.util.*

class JobPostingRepository(private val jobDao: JobDao) {

    val allJobPostings: Flow<List<JobPosting>> = jobDao.getAllJobPostings()
    val unreadJobPostings: Flow<List<JobPosting>> = jobDao.getUnreadJobPostings()
    val unreadCount: Flow<Int> = jobDao.getUnreadCount()
    val favoriteJobPostings: Flow<List<JobPosting>> = jobDao.getFavoriteJobPostings()

    fun getJobPostingsBySource(source: String): Flow<List<JobPosting>> {
        return jobDao.getJobPostingsBySource(source)
    }

    fun getJobPostingsByDateRange(startDate: Date, endDate: Date): Flow<List<JobPosting>> {
        return jobDao.getJobPostingsByDateRange(startDate.time, endDate.time)
    }

    fun getJobPostingsByApplicationStatus(status: ApplicationStatus): Flow<List<JobPosting>> {
        return jobDao.getJobPostingsByApplicationStatus(status)
    }

    @WorkerThread
    suspend fun insert(jobPosting: JobPosting) {
        jobDao.insert(jobPosting)
    }

    @WorkerThread
    suspend fun update(jobPosting: JobPosting) {
        jobDao.update(jobPosting)
    }

    @WorkerThread
    suspend fun delete(jobPosting: JobPosting) {
        jobDao.delete(jobPosting)
    }

    @WorkerThread
    suspend fun deleteMultiple(ids: List<Long>) {
        jobDao.deleteByIds(ids)
    }

    @WorkerThread
    suspend fun deleteById(id: Long) {
        jobDao.deleteById(id)
    }

    @WorkerThread
    suspend fun markAsRead(id: Long) {
        jobDao.markAsRead(id)
    }

    @WorkerThread
    suspend fun markMultipleAsRead(ids: List<Long>) {
        jobDao.markMultipleAsRead(ids)
    }

    @WorkerThread
    suspend fun updateApplicationStatus(id: Long, status: ApplicationStatus) {
        jobDao.updateApplicationStatus(id, status)
    }

    @WorkerThread
    suspend fun updateMultipleApplicationStatus(ids: List<Long>, status: ApplicationStatus) {
        jobDao.updateMultipleApplicationStatus(ids, status)
    }

    @WorkerThread
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        jobDao.updateFavoriteStatus(id, isFavorite)
    }
}