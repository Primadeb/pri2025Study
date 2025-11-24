package com.pri2025.pri2025study.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface DeadlineDao {

    @Query("SELECT * FROM deadlines ORDER BY id DESC")
    suspend fun getAll(): List<Deadline>

    @Insert
    suspend fun insert(deadline: Deadline): Long

    @Delete
    suspend fun delete(deadline: Deadline)
}
