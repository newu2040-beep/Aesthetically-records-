package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.RecordingEntity
import com.example.data.model.TemplateEntity
import kotlinx.coroutines.flow.Flow

class RecordingsRepository(private val database: AppDatabase) {

    private val recordingDao = database.recordingDao()
    private val templateDao = database.templateDao()

    val allRecordings: Flow<List<RecordingEntity>> = recordingDao.getAllRecordings()
    val favoriteRecordings: Flow<List<RecordingEntity>> = recordingDao.getFavoriteRecordings()
    val lockedRecordings: Flow<List<RecordingEntity>> = recordingDao.getLockedRecordings()
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()

    suspend fun getRecordingById(id: String): RecordingEntity? = recordingDao.getRecordingById(id)

    suspend fun insertRecording(recording: RecordingEntity) = recordingDao.insertRecording(recording)

    suspend fun updateRecording(recording: RecordingEntity) = recordingDao.updateRecording(recording)

    suspend fun deleteRecording(recording: RecordingEntity) = recordingDao.deleteRecording(recording)

    suspend fun deleteRecordingById(id: String) = recordingDao.deleteRecordingById(id)

    suspend fun toggleFavorite(recording: RecordingEntity) {
        recordingDao.updateRecording(recording.copy(isFavorite = !recording.isFavorite))
    }

    suspend fun updateLock(recording: RecordingEntity, isLocked: Boolean, pin: String) {
        recordingDao.updateRecording(recording.copy(isLocked = isLocked, pinCode = pin))
    }

    suspend fun renameRecording(recording: RecordingEntity, newTitle: String, newCategory: String, newNote: String) {
        recordingDao.updateRecording(recording.copy(title = newTitle, category = newCategory, note = newNote))
    }

    suspend fun saveCustomTemplate(template: TemplateEntity) {
        templateDao.insertTemplate(template)
    }

    suspend fun getTemplateById(id: String): TemplateEntity? = templateDao.getTemplateById(id)
}
