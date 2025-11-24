package com.pri2025.pri2025study.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insert(session: StudySession)

    // Sum minutes grouped by dayIndex (for weekly graph)
    @Query("SELECT dayIndex, SUM(minutes) AS totalMinutes FROM study_sessions GROUP BY dayIndex")
    suspend fun getTotalsByDay(): List<DayTotal>
}

// Helper class for the above query
data class DayTotal(
    val dayIndex: Int,
    val totalMinutes: Int
)
