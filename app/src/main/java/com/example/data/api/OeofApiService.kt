package com.example.data.api

import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Serialization response formats matching Node/Express backend schemas
data class AuthRequest(
    val email: String,
    val username: String,
    val password: String = "password123",
    val name: String,
    val territory: String,
    val flagEmoji: String,
    val traits: List<String>
)

data class LoginRequest(
    val emailOrUsername: String,
    val password: String = "password123"
)

data class NetworkUser(
    @Json(name = "userId") val userId: String,
    @Json(name = "email") val email: String,
    @Json(name = "username") val username: String,
    @Json(name = "name") val name: String,
    @Json(name = "territory") val territory: String,
    @Json(name = "flagEmoji") val flagEmoji: String,
    @Json(name = "rank") val rank: String,
    @Json(name = "knowledgeCredits") val knowledgeCredits: Int,
    @Json(name = "contributionCredits") val contributionCredits: Int,
    @Json(name = "reputation") val reputation: Int,
    @Json(name = "legacyScore") val legacyScore: Int,
    @Json(name = "activeSlots") val activeSlots: List<String> = emptyList(),
    @Json(name = "queuedSlots") val queuedSlots: List<String> = emptyList()
)

data class AuthResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "accessToken") val accessToken: String?,
    @Json(name = "refreshToken") val refreshToken: String?,
    @Json(name = "user") val user: NetworkUser
)

data class NetworkPost(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String,
    @Json(name = "username") val username: String,
    @Json(name = "avatarInitials") val avatarInitials: String,
    @Json(name = "territory") val territory: String,
    @Json(name = "flag") val flag: String,
    @Json(name = "rank") val rank: String,
    @Json(name = "message") val message: String,
    @Json(name = "type") val type: String,
    @Json(name = "timeAgo") val timeAgo: String = "Just now",
    @Json(name = "wiseCount") val wiseCount: Int = 0,
    @Json(name = "helpfulCount") val helpfulCount: Int = 0,
    @Json(name = "inspiringCount") val inspiringCount: Int = 0,
    @Json(name = "creativeCount") val creativeCount: Int = 0,
    @Json(name = "valuableCount") val valuableCount: Int = 0,
    @Json(name = "commentsCount") val commentsCount: Int = 0,
    @Json(name = "isPoll") val isPoll: Boolean = false,
    @Json(name = "pollOptions") val pollOptions: List<String> = emptyList(),
    @Json(name = "pollVotes") val pollVotes: List<Int> = emptyList()
)

data class FeedResponse(
    @Json(name = "posts") val posts: List<NetworkPost>,
    @Json(name = "hasMore") val hasMore: Boolean
)

data class CreatePostRequest(
    val userId: String,
    val message: String,
    val type: String,
    val isPoll: Boolean = false,
    val pollOptions: List<String> = emptyList()
)

data class CreatePostResponse(
    val success: Boolean,
    val post: NetworkPost
)

data class ReactRequest(
    val reactionType: String,
    val userId: String
)

data class ReactResponse(
    val success: Boolean,
    val post: NetworkPost,
    val updatedUser: NetworkUser?
)

data class NetworkCandidate(
    val id: String,
    val name: String,
    val flag: String,
    val rank: String,
    val manifesto: String,
    val votes: Int,
    val normalizedKc: Double,
    val reputationScore: Double
)

data class CandidatesResponse(
    val candidates: List<NetworkCandidate>
)

data class VoteRequest(
    val candidateId: String,
    val voterId: String
)

data class VoteResponse(
    val success: Boolean,
    val candidate: NetworkCandidate,
    val voter: NetworkUser
)

data class ConnectionSlotsResponse(
    val active: List<String>,
    val queued: List<String>
)

data class NetworkNotification(
    val id: Int,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean
)

data class NotificationsResponse(
    val notifications: List<NetworkNotification>
)

interface OeofApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("/api/posts")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): FeedResponse

    @POST("/api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): CreatePostResponse

    @POST("/api/posts/{postId}/react")
    suspend fun reactToPost(
        @Path("postId") postId: String,
        @Body request: ReactRequest
    ): ReactResponse

    @GET("/api/elections/candidates")
    suspend fun getCandidates(): CandidatesResponse

    @POST("/api/elections/vote")
    suspend fun castVote(@Body request: VoteRequest): VoteResponse

    @GET("/api/connections/{userId}/slots")
    suspend fun getConnectionSlots(@Path("userId") userId: String): ConnectionSlotsResponse

    @POST("/api/connections/archive")
    suspend fun archiveChatSlot(@Body body: Map<String, String>): Map<String, Any>

    @GET("/api/notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: String): NotificationsResponse

    @POST("/api/notifications/{userId}/clear")
    suspend fun clearNotifications(@Path("userId") userId: String): Map<String, Any>

    companion object {
        // Points to loopback inside streaming environment, fallback to production app service
        private const val LOCAL_URL = "http://10.0.2.2:8080"
        private const val CLOUD_URL = "https://ais-dev-ngjpdhvruudgmksomaoe5y-886256648460.asia-east1.run.app"

        fun create(customBaseUrl: String? = null): OeofApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val baseUrl = customBaseUrl ?: CLOUD_URL

            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl("$baseUrl/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OeofApiService::class.java)
        }
    }
}
