package com.example.data

import kotlinx.coroutines.flow.Flow

class OeofRepository(
    private val notificationDao: NotificationDao,
    private val userStatsDao: UserStatsDao
) {
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStats()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: Int) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    suspend fun saveUserStats(userStats: UserStatsEntity) {
        userStatsDao.insertUserStats(userStats)
    }

    suspend fun fetchOrCreateUser(): UserStatsEntity {
        val existing = userStatsDao.getUserStatsSingle()
        if (existing != null) {
            return existing
        }
        val defaultUser = UserStatsEntity()
        userStatsDao.insertUserStats(defaultUser)
        return defaultUser
    }
}
