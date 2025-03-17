package com.harshsinghio.fresherjobupdate.dao

import androidx.room.*
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.model.JobPosting
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface JobDao {
    @Query("SELECT * FROM job_postings ORDER BY timestamp DESC")
    fun getAllJobPostings(): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadJobPostings(): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE source = :source ORDER BY timestamp DESC")
    fun getJobPostingsBySource(source: String): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getJobPostingsByDateRange(startDate: Long, endDate: Long): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE applicationStatus = :status ORDER BY timestamp DESC")
    fun getJobPostingsByApplicationStatus(status: ApplicationStatus): Flow<List<JobPosting>>

    @Query("SELECT * FROM job_postings WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteJobPostings(): Flow<List<JobPosting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobPosting: JobPosting)

    @Update
    suspend fun update(jobPosting: JobPosting)

    @Delete
    suspend fun delete(jobPosting: JobPosting)

    @Query("DELETE FROM job_postings WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM job_postings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE job_postings SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE job_postings SET isRead = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsRead(ids: List<Long>)

    @Query("UPDATE job_postings SET applicationStatus = :status WHERE id = :id")
    suspend fun updateApplicationStatus(id: Long, status: ApplicationStatus)

    @Query("UPDATE job_postings SET applicationStatus = :status WHERE id IN (:ids)")
    suspend fun updateMultipleApplicationStatus(ids: List<Long>, status: ApplicationStatus)

    @Query("UPDATE job_postings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM job_postings WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}