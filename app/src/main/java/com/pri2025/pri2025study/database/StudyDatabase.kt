package com.pri2025.pri2025study.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudySession::class,
        Deadline::class,
        StudySettings::class
    ],
    version = 3,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao
    abstract fun deadlineDao(): DeadlineDao
    abstract fun studySettingsDao(): StudySettingsDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_db"
                )
                    .fallbackToDestructiveMigration()   // ok for dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
