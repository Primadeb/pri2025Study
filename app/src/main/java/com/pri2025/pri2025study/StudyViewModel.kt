package com.pri2025.pri2025study

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pri2025.pri2025study.database.StudyDatabase
import com.pri2025.pri2025study.database.StudySettings
import kotlinx.coroutines.launch

class StudyViewModel(
    private val db: StudyDatabase
) : ViewModel() {

    var quickAddMinutes by mutableStateOf(30)
        private set

    var focusTimeMinutes by mutableStateOf(30)
        private set

    init {
        viewModelScope.launch {
            val settings = db.studySettingsDao().getSettings()
            if (settings != null) {
                quickAddMinutes = settings.quickAddMinutes
                focusTimeMinutes = settings.focusTimeMinutes
            } else {
                saveSettings()
            }
        }
    }

    fun updateQuickAddMinutes(value: Int) {
        quickAddMinutes = value
        saveSettings()
    }

    fun updateFocusTimeMinutes(value: Int) {
        focusTimeMinutes = value
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val settings = StudySettings(
                id = 0,
                quickAddMinutes = quickAddMinutes,
                focusTimeMinutes = focusTimeMinutes
            )
            db.studySettingsDao().saveSettings(settings)
        }
    }
}
