package com.pri2025.pri2025study.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row table to store app settings (focus time + quick add)
@Entity(tableName = "study_settings")
data class StudySettings(
    @PrimaryKey val id: Int = 0,          // always 0 = only one row
    val quickAddMinutes: Int = 30,
    val focusTimeMinutes: Int = 30
)
