package com.pri2025.pri2025study.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StudySettingsDao {

    @Query("SELECT * FROM study_settings WHERE id = 0 LIMIT 1")
    suspend fun getSettings(): StudySettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: StudySettings)
}
