package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 'current_user' LIMIT 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 'current_user' LIMIT 1")
    suspend fun getUserStatsSingle(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(userStats: UserStatsEntity)
}
