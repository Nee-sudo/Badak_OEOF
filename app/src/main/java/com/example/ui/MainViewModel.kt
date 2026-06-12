package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NotificationEntity
import com.example.data.OeofRepository
import com.example.data.UserStatsEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    ONBOARDING,
    REGISTRATION,
    TERRITORY_SELECTION,
    PERSONALITY_SELECTION,
    OATH,
    MAIN_APP
}

enum class Tab {
    HOME,
    SQUARE,
    ARENA,
    MESSAGES,
    EMPIRE,
    PROFILE
}

// Data models for the interactive features
data class Post(
    val id: String,
    val author: String,
    val username: String,
    val avatarInitials: String,
    val territory: String,
    val flag: String,
    val rank: String,
    val message: String,
    val type: String, // Public Square, Civic Poll, Article, Project
    val timeAgo: String,
    val wiseCount: Int = 0,
    val helpfulCount: Int = 0,
    val inspiringCount: Int = 0,
    val creativeCount: Int = 0,
    val valuableCount: Int = 0,
    val commentsCount: Int = 0,
    val isPoll: Boolean = false,
    val pollOptions: List<String> = emptyList(),
    val pollVotes: List<Int> = emptyList(),
    var selectedOption: Int? = null
)

data class Candidate(
    val id: String,
    val name: String,
    val flag: String,
    val rank: String,
    val manifesto: String,
    val votes: Int,
    val normalizedKc: Double,
    val reputationScore: Double
) {
    // Leadership Score = 70% normalized credits/merit + 30% voting mandate
    val leadershipScore: Double
        get() = (normalizedKc * 0.70) + ((votes * 3.0) * 0.30)
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class ChatConnection(
    val id: String,
    val name: String,
    val flag: String,
    val rank: String,
    val lastMessage: String,
    val avatarColor: String,
    var isArchived: Boolean = false
)

class MainViewModel(private val repository: OeofRepository) : ViewModel() {

    // Main navigation flow state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _onboardingSlide = MutableStateFlow(0)
    val onboardingSlide: StateFlow<Int> = _onboardingSlide.asStateFlow()

    private val _activeTab = MutableStateFlow(Tab.HOME)
    val activeTab: StateFlow<Tab> = _activeTab.asStateFlow()

    // Interactive view layers
    private val _showingNotificationCenter = MutableStateFlow(false)
    val showingNotificationCenter: StateFlow<Boolean> = _showingNotificationCenter.asStateFlow()

    private val _showingSettings = MutableStateFlow(false)
    val showingSettings: StateFlow<Boolean> = _showingSettings.asStateFlow()

    private val _showingCreatePost = MutableStateFlow(false)
    val showingCreatePost: StateFlow<Boolean> = _showingCreatePost.asStateFlow()

    private val _showingExitSummary = MutableStateFlow(false)
    val showingExitSummary: StateFlow<Boolean> = _showingExitSummary.asStateFlow()

    // Selected theme: "Light", "Dark", "System"
    private val _appearanceTheme = MutableStateFlow("Light")
    val appearanceTheme: StateFlow<String> = _appearanceTheme.asStateFlow()

    // Temporary registration fields during onboarding
    val regName = MutableStateFlow("")
    val regUsername = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regPassword = MutableStateFlow("")
    val regBirthday = MutableStateFlow("")

    val selectedTerritory = MutableStateFlow("India")
    val selectedFlag = MutableStateFlow("🇮🇳")
    private val _selectedTraits = MutableStateFlow<Set<String>>(emptySet())
    val selectedTraits: StateFlow<Set<String>> = _selectedTraits.asStateFlow()

    // Room Persistent states exposed
    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks statistics accumulated during the active gaming session (for Exit Summary screen)
    val sessionKcEarned = MutableStateFlow(0)
    val sessionCcEarned = MutableStateFlow(0)
    val sessionVisitorsToday = MutableStateFlow(14)

    // Interactive Quiz / Knowledge Arena State
    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val isAnswerCorrect: StateFlow<Boolean?> = _isAnswerCorrect.asStateFlow()

    // Active connection slot list (Max 3) & Queued queue chat list
    private val _connections = MutableStateFlow<List<ChatConnection>>(emptyList())
    val connections: StateFlow<List<ChatConnection>> = _connections.asStateFlow()

    private val _queuedChatRequests = MutableStateFlow<List<ChatConnection>>(emptyList())
    val queuedChatRequests: StateFlow<List<ChatConnection>> = _queuedChatRequests.asStateFlow()

    // Live Feed posts list
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Election Candidates list
    private val _candidates = MutableStateFlow<List<Candidate>>(emptyList())
    val candidates: StateFlow<List<Candidate>> = _candidates.asStateFlow()

    // Question bank for Arena
    val quizQuestions = listOf(
        QuizQuestion(
            question = "Which system operates most efficiently in low-gravity thermal dissipation?",
            options = listOf("Convective copper-mesh loop", "Liquid helium phase-change system", "Carbon nanotube radiant fins", "Graphene heat-pipe capillary pump"),
            correctAnswerIndex = 2,
            explanation = "Carbon nanotube radiant fins dissipate thermal radiation highly effectively in near-vacuum or micro-gravity conditions due to extremely high surface-area-to-weight ratios."
        ),
        QuizQuestion(
            question = "What economic theory promotes merit-based currency distribution over speculative trading?",
            options = listOf("Classical Keynesianism", "Value-Contribution Paradigm", "Universal Basic Inflation model", "Laissez-faire Meritocracy"),
            correctAnswerIndex = 1,
            explanation = "The Value-Contribution Paradigm bases circulating tokens directly upon verified scientific research, mentorship, and civic works, bypassing standard speculative loops."
        ),
        QuizQuestion(
            question = "In quantum mechanics, which phenomenon enables the sharing of spin state correlation?",
            options = listOf("Quantum Superposition", "Quantum Tunneling", "Quantum Entanglement", "Quantum Coherence"),
            correctAnswerIndex = 2,
            explanation = "Quantum Entanglement couples particle spin states instantly over arbitrary distances, a core principle tested in peer-reviewed Arena modules."
        )
    )

    init {
        // Initialize Core Seed data
        viewModelScope.launch {
            repository.fetchOrCreateUser()
            seedCoreContent()
        }
    }

    private fun seedCoreContent() {
        // Core Posts
        _posts.value = listOf(
            Post(
                id = "post_1",
                author = "Aryan Sharma",
                username = "aryan.s",
                avatarInitials = "AS",
                territory = "India",
                flag = "🇮🇳",
                rank = "Duke",
                message = "Just finished designing an open-source decentralized smart-grid framework for rural Territories. This leverages low-cost photovoltaic arrays paired with micro-capacitors to guarantee energy security to 50+ local households. Please review our schematic, peer references are welcome!",
                type = "Project",
                timeAgo = "2h ago",
                wiseCount = 128,
                helpfulCount = 64,
                inspiringCount = 31,
                creativeCount = 12,
                valuableCount = 8,
                commentsCount = 24
            ),
            Post(
                id = "post_2",
                author = "Yumi Tanaka",
                username = "yumi.t",
                avatarInitials = "YT",
                territory = "Japan",
                flag = "🇯🇵",
                rank = "Baroness",
                message = "Should the Empire initiate and prioritize a unified digital school in sub-Saharan Territories this season? Cast your votes below. This plan integrates 1,000+ retired educators across Asia and Europe to mentor kids over voice nodes.",
                type = "Civic Poll",
                timeAgo = "5h ago",
                wiseCount = 85,
                helpfulCount = 52,
                commentsCount = 42,
                isPoll = true,
                pollOptions = listOf("Yes, allocate global fund", "No, focus on local grids"),
                pollVotes = listOf(72, 28)
            ),
            Post(
                id = "post_3",
                author = "Elena Reyes",
                username = "elena.r",
                avatarInitials = "ER",
                territory = "Spain",
                flag = "🇪🇸",
                rank = "Noble",
                message = "The Economics of Open Knowledge: A brief dissertation on why intellectual credits outperform monetary speculation. Knowledge is non-rivalrous; when shared, its systemic utility compiles exponentially.",
                type = "Article",
                timeAgo = "1d ago",
                wiseCount = 210,
                helpfulCount = 98,
                commentsCount = 14
            )
        )

        // Messaging - Slot connections (Limit of 3: Three-Connection Rule)
        _connections.value = listOf(
            ChatConnection("chat_yumi", "Yumi Tanaka", "🇯🇵", "Baroness", "Aryan, I just sent the finalized manifesto draft for the agricultural project.", "#1E3A8A"),
            ChatConnection("chat_elena", "Elena Reyes", "🇪🇸", "Noble", "The solar schools proposal is passing with great statistics! Check India stand.", "#1F9D55"),
            ChatConnection("chat_kofi", "Kofi Mensah", "🇬🇭", "Guardian", "The vote counts in Ghana are looking incredibly strong. We rise!", "#F2A900")
        )

        // Messaging - Waiting Queue
        _queuedChatRequests.value = listOf(
            ChatConnection("chat_queued_1", "Sophie Laurent", "🇫🇷", "Contributor", "Requested to consult you on neural network models.", "#2D9CDB"),
            ChatConnection("chat_queued_2", "Mateo Silva", "🇧🇷", "Noble", "Wishes to establish alliance for forestry index.", "#D4AF37")
        )

        // Election Candidates
        _candidates.value = listOf(
            Candidate("cand_aryan", "Aryan Sharma", "🇮🇳", "Duke", "A future where merit and compassion govern every Territory equally through open blockchain ledgers.", 142, 92.5, 98.0),
            Candidate("cand_yumi", "Yumi Tanaka", "🇯🇵", "Baroness", "Knowledge for every citizen, transparency for every Territory, global mentorship classrooms.", 124, 88.0, 96.0),
            Candidate("cand_elena", "Elena Reyes", "🇪🇸", "Noble", "Harmonized resource redistribution and digital library access for developing schools.", 85, 82.3, 94.0)
        )
    }

    // Navigation and Carousel Flow Control
    fun startCampaign() {
        _currentScreen.value = Screen.ONBOARDING
        _onboardingSlide.value = 0
    }

    fun nextOnboarding() {
        if (_onboardingSlide.value < 2) {
            _onboardingSlide.value += 1
        } else {
            _currentScreen.value = Screen.REGISTRATION
        }
    }

    fun completeRegistration() {
        if (regName.value.isNotBlank() && regUsername.value.isNotBlank() && regEmail.value.isNotBlank()) {
            _currentScreen.value = Screen.TERRITORY_SELECTION
        }
    }

    fun selectTerritory(name: String, flag: String) {
        selectedTerritory.value = name
        selectedFlag.value = flag
    }

    fun confirmTerritory() {
        _currentScreen.value = Screen.PERSONALITY_SELECTION
    }

    fun toggleTrait(trait: String) {
        val current = _selectedTraits.value.toMutableSet()
        if (current.contains(trait)) {
            current.remove(trait)
        } else {
            if (current.size < 5) {
                current.add(trait)
            }
        }
        _selectedTraits.value = current
    }

    fun confirmTraits() {
        _currentScreen.value = Screen.OATH
    }

    fun acceptOath() {
        viewModelScope.launch {
            // Write completed, verified citizen info to Room DB
            val user = UserStatsEntity(
                name = regName.value.ifBlank { "Aryan Sharma" },
                username = regUsername.value.ifBlank { "aryan.s" },
                territory = selectedTerritory.value,
                flagEmoji = selectedFlag.value,
                rank = "Citizen",
                knowledgeCredits = 10,  // Seed with standard welcome credits
                contributionCredits = 5,
                reputation = 98,
                legacyScore = 1,
                personalityTraits = _selectedTraits.value.joinToString(",")
            )
            repository.saveUserStats(user)

            // Trigger a welcome notification
            repository.insertNotification(
                NotificationEntity(
                    title = "Verified Citizenship Granted",
                    body = "Welcome to the digital civilization, Citizen @${user.username}! Your oath of alliance is recorded.",
                    type = "PROMOTION"
                )
            )

            // Transition to dashboard!
            _currentScreen.value = Screen.MAIN_APP
            _activeTab.value = Tab.HOME
        }
    }

    // Feed interaction methods - React to post
    fun reactToPost(postId: String, reactionType: String) {
        viewModelScope.launch {
            val user = repository.fetchOrCreateUser()
            var creditsAdded = 0
            var text = ""

            val updatedPosts = _posts.value.map { post ->
                if (post.id == postId) {
                    var ws = post.wiseCount
                    var hl = post.helpfulCount
                    var ins = post.inspiringCount
                    var cr = post.creativeCount
                    var valCount = post.valuableCount

                    when (reactionType) {
                        "wise" -> {
                            ws += 1
                            creditsAdded = 5
                            text = "reacted 'Wise' to your solar schematic post (+5 KC)."
                        }
                        "helpful" -> {
                            hl += 1
                            creditsAdded = 5
                            text = "reacted 'Helpful' to your solar schematic post (+5 CC)."
                        }
                        "inspiring" -> ins += 1
                        "creative" -> cr += 1
                        "valuable" -> valCount += 1
                    }

                    post.copy(
                        wiseCount = ws,
                        helpfulCount = hl,
                        inspiringCount = ins,
                        creativeCount = cr,
                        valuableCount = valCount
                    )
                } else {
                    post
                }
            }
            _posts.value = updatedPosts

            if (creditsAdded > 0) {
                if (reactionType == "wise") {
                    val newKc = user.knowledgeCredits + creditsAdded
                    sessionKcEarned.value += creditsAdded
                    val newRank = calculateRankGrading(newKc, user.contributionCredits)
                    repository.saveUserStats(user.copy(knowledgeCredits = newKc, rank = newRank))

                    // Insert database notification
                    repository.insertNotification(
                        NotificationEntity(
                            title = "Knowledge Credits compilation",
                            body = "Scholar Yumi Tanaka $text",
                            type = "REACTION"
                        )
                    )
                } else if (reactionType == "helpful") {
                    val newCc = user.contributionCredits + creditsAdded
                    sessionCcEarned.value += creditsAdded
                    val newRank = calculateRankGrading(user.knowledgeCredits, newCc)
                    repository.saveUserStats(user.copy(contributionCredits = newCc, rank = newRank))

                    // Insert database notification
                    repository.insertNotification(
                        NotificationEntity(
                            title = "Contribution compilation",
                            body = "Scholar Yumi Tanaka $text",
                            type = "REACTION"
                        )
                    )
                }
            }
        }
    }

    // Simple rank escalation algorithm based on credit metrics
    private fun calculateRankGrading(kc: Int, cc: Int): String {
        return when {
            kc >= 50 && cc >= 30 -> "Noble"
            kc >= 30 && cc >= 15 -> "Guardian"
            kc >= 15 && cc >= 5 -> "Contributor"
            else -> "Citizen"
        }
    }

    // Compose new post
    fun publishPost(content: String, type: String) {
        viewModelScope.launch {
            val user = repository.fetchOrCreateUser()
            val newPost = Post(
                id = "post_${System.currentTimeMillis()}",
                author = user.name,
                username = user.username,
                avatarInitials = user.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase(),
                territory = user.territory,
                flag = user.flagEmoji,
                rank = user.rank,
                message = content,
                type = type,
                timeAgo = "Just now"
            )
            _posts.value = listOf(newPost) + _posts.value
            _showingCreatePost.value = false

            // Create notification confirming publication
            repository.insertNotification(
                NotificationEntity(
                    title = "Article Published in Square",
                    body = "Your ${type.lowercase()} got broadcasted to Territory of ${user.territory}.",
                    type = "EMPIRE"
                )
            )
        }
    }

    // Cast vote in active elections
    fun castElectionVote(candidateId: String) {
        viewModelScope.launch {
            val user = repository.fetchOrCreateUser()
            _candidates.value = _candidates.value.map { cand ->
                if (cand.id == candidateId) {
                    cand.copy(votes = cand.votes + 1)
                } else {
                    cand
                }
            }
            // Add CC rewards for fulfilling general citizen voting duty!
            val newCc = user.contributionCredits + 10
            sessionCcEarned.value += 10
            val newRank = calculateRankGrading(user.knowledgeCredits, newCc)
            repository.saveUserStats(user.copy(contributionCredits = newCc, rank = newRank))

            repository.insertNotification(
                NotificationEntity(
                    title = "Vote Confirmed",
                    body = "You successfully submitted your general vote. Earning +10 cc as civic compliance incentive.",
                    type = "ELECTION"
                )
            )
        }
    }

    // Arena / Quiz functions
    fun selectQuizAnswer(index: Int) {
        if (_selectedAnswerIndex.value != null || _quizCompleted.value) return
        _selectedAnswerIndex.value = index
        val currentQuestion = quizQuestions[_currentQuizIndex.value]
        val isCorrect = index == currentQuestion.correctAnswerIndex
        _isAnswerCorrect.value = isCorrect

        viewModelScope.launch {
            val user = repository.fetchOrCreateUser()
            if (isCorrect) {
                // Award knowledge credits!
                val reward = 15
                val newKc = user.knowledgeCredits + reward
                sessionKcEarned.value += reward
                val newRank = calculateRankGrading(newKc, user.contributionCredits)
                repository.saveUserStats(user.copy(knowledgeCredits = newKc, rank = newRank))

                repository.insertNotification(
                    NotificationEntity(
                        title = "Wise Arena Solution!",
                        body = "You resolved the Quantum Mechanics core puzzle! Awarded +15 Knowledge Credits.",
                        type = "REACTION"
                    )
                )
            } else {
                repository.insertNotification(
                    NotificationEntity(
                        title = "Arena Quiz review",
                        body = "Attempt submitted. Read the peer-reviewed carbon dissipation explanation.",
                        type = "REACTION"
                    )
                )
            }
        }
    }

    fun nextQuizQuestion() {
        _selectedAnswerIndex.value = null
        _isAnswerCorrect.value = null
        if (_currentQuizIndex.value < quizQuestions.size - 1) {
            _currentQuizIndex.value += 1
        } else {
            _quizCompleted.value = true
        }
    }

    fun resetQuiz() {
        _currentQuizIndex.value = 0
        _selectedAnswerIndex.value = null
        _isAnswerCorrect.value = null
        _quizCompleted.value = false
    }

    // Messaging Three-Connections Rule Management
    fun archiveChat(chatId: String) {
        viewModelScope.launch {
            // Find in active contacts and toggle isArchived
            _connections.value = _connections.value.filter { it.id != chatId }

            // Pull first chat from waiting queue (if queue is not empty)
            val queue = _queuedChatRequests.value.toMutableList()
            if (queue.isNotEmpty()) {
                val nextActive = queue.removeAt(0)
                _connections.value = _connections.value + nextActive
                _queuedChatRequests.value = queue

                repository.insertNotification(
                    NotificationEntity(
                        title = "Queue Chat slot activated",
                        body = "Chat invitation from Scholar ${nextActive.name} ${nextActive.flag} moved to Active list.",
                        type = "MESSAGE"
                    )
                )
            }
        }
    }

    // Appearance State Toggle
    fun updateTheme(themeName: String) {
        _appearanceTheme.value = themeName
    }

    // Notification manipulation
    fun createMockNotification() {
        viewModelScope.launch {
            val visitors = listOf(
                Pair("Sophia Laurent", "🇫🇷"),
                Pair("Mateo Silva", "🇧🇷"),
                Pair("Kofi Mensah", "🇬🇭"),
                Pair("Hans Mueller", "🇩🇪")
            )
            val randomVisitor = visitors.random()

            val notificationsSet = listOf(
                NotificationEntity(
                    title = "Profile Visitor logged",
                    body = "Scholar ${randomVisitor.first} from Territory of ${randomVisitor.second} visited your citizen profile.",
                    type = "VISITOR"
                ),
                NotificationEntity(
                    title = "Territory Rank Update",
                    body = "Territory of ${selectedTerritory.value} ${selectedFlag.value} rose to Rank #3 worldwide.",
                    type = "EMPIRE"
                ),
                NotificationEntity(
                    title = "General Election Countdown",
                    body = "General elections commence in 2 days. Candidates are delivering campaigns inside the Palace.",
                    type = "ELECTION"
                )
            )

            val finalNotification = notificationsSet.random()
            repository.insertNotification(finalNotification)

            if (finalNotification.type == "VISITOR") {
                sessionVisitorsToday.value += 1
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Screen controllers
    fun selectTab(tab: Tab) {
        _activeTab.value = tab
    }

    fun setShowingNotificationCenter(show: Boolean) {
        _showingNotificationCenter.value = show
    }

    fun setShowingSettings(show: Boolean) {
        _showingSettings.value = show
    }

    fun setShowingCreatePost(show: Boolean) {
        _showingCreatePost.value = show
    }

    fun setShowingExitSummary(show: Boolean) {
        _showingExitSummary.value = show
    }

    // Complete session reset (logout)
    fun executeLogout() {
        viewModelScope.launch {
            // Reset state
            _showingExitSummary.value = false
            _showingCreatePost.value = false
            _showingSettings.value = false
            _showingNotificationCenter.value = false
            _currentScreen.value = Screen.SPLASH
            sessionKcEarned.value = 0
            sessionCcEarned.value = 0
            sessionVisitorsToday.value = 14
            resetQuiz()
            _activeTab.value = Tab.HOME
            seedCoreContent()
        }
    }
}
