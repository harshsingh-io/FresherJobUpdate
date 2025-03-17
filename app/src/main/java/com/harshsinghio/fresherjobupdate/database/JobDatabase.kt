package com.harshsinghio.fresherjobupdate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harshsinghio.fresherjobupdate.dao.JobDao
import com.harshsinghio.fresherjobupdate.model.JobPosting

@Database(entities = [JobPosting::class], version = 2, exportSchema = false)
abstract class JobDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao

    companion object {
        @Volatile
        private var INSTANCE: JobDatabase? = null

        fun getDatabase(context: Context): JobDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JobDatabase::class.java,
                    "job_posting_database"
                )
                    .fallbackToDestructiveMigration() // This will destroy and recreate the database when version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}