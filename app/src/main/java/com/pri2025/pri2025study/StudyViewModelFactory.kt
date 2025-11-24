package com.pri2025.pri2025study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pri2025.pri2025study.database.StudyDatabase

class StudyViewModelFactory(
    private val db: StudyDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
