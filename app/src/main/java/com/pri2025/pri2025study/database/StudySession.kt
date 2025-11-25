package com.pri2025.pri2025study.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Each row is one study chunk (e.g. quick add or timer)
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayIndex: Int,    // 0 = Mon ... 6 = Sun
    val minutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
