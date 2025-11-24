package com.pri2025.pri2025study.database


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deadlines")
data class Deadline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueText: String
)

