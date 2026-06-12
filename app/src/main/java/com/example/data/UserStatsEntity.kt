package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Aryan Sharma",
    val username: String = "aryan.s",
    val territory: String = "India",
    val flagEmoji: String = "🇮🇳",
    val rank: String = "Citizen", // Citizen, Contributor, Guardian, Noble, Baron, Duke, Prince, Royal Candidate, King
    val knowledgeCredits: Int = 0,
    val contributionCredits: Int = 0,
    val reputation: Int = 98,
    val legacyScore: Int = 10,
    val personalityTraits: String = "Explorer,Creator,Teacher,Scientist,Leader", // Comma-separated list
    val bio: String = "Dedicated citizen of the digital civilization.",
    val skills: String = "Quantum Physics, Engineering, Philosophy",
    val interests: String = "Education, Clean Energy, Space Exploration",
    val avatarUrl: String = "",
    val coverUrl: String = ""
)
