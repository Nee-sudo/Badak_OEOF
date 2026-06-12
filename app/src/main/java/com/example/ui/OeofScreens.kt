package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.NotificationEntity
import com.example.ui.theme.*

// ============================================
// MAIN ROUTER VIEW
// ============================================
@Composable
fun OeofMainRouter(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeChoice by viewModel.appearanceTheme.collectAsState()

    // Control system level or user level dark theme setting
    val isDarkTheme = when (themeChoice) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val focusManager = LocalFocusManager.current
    MyApplicationTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.SPLASH -> SplashScreenView(viewModel)
                    Screen.ONBOARDING -> OnboardingCarouselView(viewModel)
                    Screen.REGISTRATION -> RegistrationView(viewModel)
                    Screen.TERRITORY_SELECTION -> TerritorySelectionView(viewModel)
                    Screen.PERSONALITY_SELECTION -> PersonalitySelectionView(viewModel)
                    Screen.OATH -> CitizenOathView(viewModel)
                    Screen.MAIN_APP -> MainAppLayout(viewModel)
                }
            }
        }
    }
}

// ============================================
// SCREEN 01: SPLASH SCREEN
// ============================================
@Composable
fun SplashScreenView(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1C2750), Color(0xFF0A0B14)),
                    radius = 1200f
                )
            )
            .safeDrawingPadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "OEOF Crest",
                tint = GoldAccent,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(2.dp, Color(0xFF2C3A73), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Globe",
                    tint = RoyalBlueLight,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ONE EARTH",
                fontSize = 12.sp,
                color = DarkTextSecondary,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ONE FAMILY",
                fontSize = 12.sp,
                color = DarkTextSecondary,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ENTER THE EMPIRE",
                fontSize = 14.sp,
                color = GoldAccent,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.startCampaign() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = TextOnGold
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENTER THE EMPIRE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

// ============================================
// SCREEN 02: ONBOARDING CAROUSEL
// ============================================
@Composable
fun OnboardingCarouselView(viewModel: MainViewModel) {
    val slide by viewModel.onboardingSlide.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { viewModel.completeRegistration() }) {
                Text("Skip", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (slide) {
                        0 -> Icons.Default.MenuBook
                        1 -> Icons.Default.VerifiedUser
                        else -> Icons.Default.Public
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (slide) {
                    0 -> "Represent your Territory"
                    1 -> "Rise Through The Ranks"
                    else -> "Knowledge Over Popularity"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (slide) {
                    0 -> "Every country on Earth is an equal Territory of our great civilization. Represent your nation in peaceful global integration."
                    1 -> "Earn credit multipliers by completing civic missions and training. Leaders are democratically elected based strictly on true merit scores."
                    else -> "No hollow vanity metrics or outrage farming algorithms. Status comes purely from peer-verified education and tangible social contribution."
                },
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == slide) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == slide) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        Button(
            onClick = { viewModel.nextOnboarding() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                if (slide == 2) "Launch Registration" else "Continue",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

// ============================================
// SCREEN 03: REGISTRATION VIEW
// ============================================
@Composable
fun RegistrationView(viewModel: MainViewModel) {
    val name by viewModel.regName.collectAsState()
    val username by viewModel.regUsername.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val password by viewModel.regPassword.collectAsState()
    val dob by viewModel.regBirthday.collectAsState()

    var isOtpSent by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var showTerms by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Join the Civilisation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Apply for verified global citizen passport",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isOtpSent) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.regName.value = it },
                    label = { Text("Full Legal Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.regUsername.value = it },
                    label = { Text("Username") },
                    leadingIcon = { Text("@", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.regEmail.value = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.regPassword.value = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )
            }

            item {
                OutlinedTextField(
                    value = dob,
                    onValueChange = { viewModel.regBirthday.value = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = showTerms, onCheckedChange = { showTerms = it })
                    Text("I approve the platform privacy declarations", fontSize = 12.sp)
                }
            }

            item {
                Button(
                    onClick = { isOtpSent = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = name.isNotBlank() && username.isNotBlank() && email.isNotBlank() && password.length >= 6 && showTerms,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Verification OTP", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // OTP verification panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "One-Time Code Dispatched",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "We sent a 6-digit confirmation key to $email. Enter it below to issue your Citizen Passport.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { if (it.length <= 6) enteredOtp = it },
                            label = { Text("6-Digit Key") },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { isOtpSent = false }) {
                                Text("Back", color = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = { viewModel.completeRegistration() },
                                enabled = enteredOtp.length == 6
                            ) {
                                Text("Verify Key")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("By registering, you acknowledge that character ranking and legacy scores remain immutable under sovereign digital ledger regulations.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}

// ============================================
// SCREEN 04: TERRITORY SELECTION SCREEN
// ============================================
@Composable
fun TerritorySelectionView(viewModel: MainViewModel) {
    val activeTerritory by viewModel.selectedTerritory.collectAsState()
    val activeFlag by viewModel.selectedFlag.collectAsState()

    val territories = listOf(
        Triple("India", "🇮🇳", "Rank #3 global contribution"),
        Triple("Japan", "🇯🇵", "Rank #5 global contribution"),
        Triple("Brazil", "🇧🇷", "Rank #8 global contribution"),
        Triple("Kenya", "🇰🇪", "Rank #14 global contribution"),
        Triple("Germany", "🇩🇪", "Rank #6 global contribution"),
        Triple("United States", "🇺🇸", "Rank #1 global contribution")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Select Your Territory",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                "A permanent tie. You carry this flag into the Arena, feed, and central elections.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Abstract interactive vector world map simulator card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Public, contentDescription = null, size = 48.dp, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Target Territory: $activeTerritory $activeFlag",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            territories.forEach { (name, flag, rank) ->
                val isSelected = activeTerritory == name
                Card(
                    onClick = { viewModel.selectTerritory(name, flag) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(rank, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Active Selection", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        Button(
            onClick = { viewModel.confirmTerritory() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Flag, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirm Territory", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ============================================
// SCREEN 05: PERSONALITY TRAIT SETUP
// ============================================
@Composable
fun PersonalitySelectionView(viewModel: MainViewModel) {
    val selected by viewModel.selectedTraits.collectAsState()
    val traitOptions = listOf(
        "Explorer" to Icons.Default.Public,
        "Builder" to Icons.Default.Handyman,
        "Teacher" to Icons.Default.School,
        "Scientist" to Icons.Default.Science,
        "Leader" to Icons.Default.Shield,
        "Creator" to Icons.Default.Palette,
        "Artist" to Icons.Default.AutoAwesome,
        "Visionary" to Icons.Default.Psychology,
        "Humanitarian" to Icons.Default.Favorite,
        "Philosopher" to Icons.Default.Book
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Define Identity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${selected.size} / 5 Selected") }
                )
            }
            Text(
                "Choose exactly five traits. These badges display publicly on your civilian passport and dictate baseline Arena bonuses.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            traitOptions.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { (trait, icon) ->
                        val isSelected = selected.contains(trait)
                        Card(
                            onClick = { viewModel.toggleTrait(trait) },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    trait,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        Button(
            onClick = { viewModel.confirmTraits() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = selected.size == 5,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirm Traid Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

// ============================================
// SCREEN 06: CITIZEN OATH DIALOG/MODAL
// ============================================
@Composable
fun CitizenOathView(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1730).copy(alpha = 0.65f))
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    "The Citizen Oath",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    "\"I belong to One Earth and One Family. I shall respect all citizens. I shall contribute positively. I shall seek knowledge and truth. I shall help build a better future.\"",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Divider()

                Button(
                    onClick = { viewModel.acceptOath() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = TextOnGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept Oath and Enter Empire", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================
// SCREENS 07 - 17: CORE TAB SYSTEM
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val notificationCount by viewModel.notifications.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    // Overlay triggers
    val showNotifications by viewModel.showingNotificationCenter.collectAsState()
    val showSettings by viewModel.showingSettings.collectAsState()
    val showCreatePost by viewModel.showingCreatePost.collectAsState()
    val showExitSummary by viewModel.showingExitSummary.collectAsState()

    val unreadNotifications = notificationCount.filter { !it.isRead }

    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(userStats?.flagEmoji ?: "🇮🇳", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    userStats?.name ?: "Aryan Sharma",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GoldAccent),
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, size = 10.dp, tint = TextOnGold)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(userStats?.rank ?: "Citizen", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextOnGold)
                                    }
                                }
                            }
                            Text(
                                "Reputation ${userStats?.reputation ?: 98}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { viewModel.setShowingNotificationCenter(true) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                        if (unreadNotifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.setShowingSettings(true) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Identity Control")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            )
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    NavigationBarItem(
                        selected = activeTab == Tab.HOME,
                        onClick = { viewModel.selectTab(Tab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == Tab.SQUARE,
                        onClick = { viewModel.selectTab(Tab.SQUARE) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Square", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == Tab.ARENA,
                        onClick = { viewModel.selectTab(Tab.ARENA) },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                        label = { Text("Arena", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == Tab.MESSAGES,
                        onClick = { viewModel.selectTab(Tab.MESSAGES) },
                        icon = { Icon(Icons.Default.Mail, contentDescription = null) },
                        label = { Text("Chats", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == Tab.EMPIRE,
                        onClick = { viewModel.selectTab(Tab.EMPIRE) },
                        icon = { Icon(Icons.Default.TravelExplore, contentDescription = null) },
                        label = { Text("Empire", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == Tab.PROFILE,
                        onClick = { viewModel.selectTab(Tab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile", fontSize = 10.sp) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (!isKeyboardVisible && (activeTab == Tab.HOME || activeTab == Tab.SQUARE)) {
                FloatingActionButton(
                    onClick = { viewModel.setShowingCreatePost(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Tab router
            when (activeTab) {
                Tab.HOME -> FeedTabView(viewModel)
                Tab.SQUARE -> SquareTabView(viewModel)
                Tab.ARENA -> ArenaTabView(viewModel)
                Tab.MESSAGES -> MessagesTabView(viewModel)
                Tab.EMPIRE -> EmpireTabView(viewModel)
                Tab.PROFILE -> ProfileTabView(viewModel)
            }

            // Sheets/Overlays
            if (showNotifications) {
                NotificationCenterSheet(viewModel)
            }
            if (showSettings) {
                AppearanceSettingsSheet(viewModel)
            }
            if (showCreatePost) {
                CreatePostDialog(viewModel)
            }
            if (showExitSummary) {
                ExitSummaryDialog(viewModel)
            }
        }
    }
}

// ============================================
// SCREEN 07 / 08: HOME FEED TAB
// ============================================
@Composable
fun FeedTabView(viewModel: MainViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val feedPosts by viewModel.posts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Royal Welcome glassmorphism card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldAccent, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Text("Daily Royal Welcome", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Greetings, Citizen. Since dusk your profile logged 14 visits; your scholastic efforts compiled +32 credits. Territory of ${userStats?.territory ?: "India"} ${userStats?.flagEmoji} peaked at Rank #3 overall.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Active election card count label
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Ballot, contentDescription = null, tint = Color.White)
                        Text("General Election active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    AssistChip(
                        onClick = { viewModel.selectTab(Tab.EMPIRE) },
                        label = { Text("Elections", color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }

        // Post feeds items
        items(feedPosts) { post ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape = RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    post.avatarInitials,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(post.flag, fontSize = 14.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.rank, fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(" ${post.timeAgo}  •  ${post.type}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Text(
                        post.message,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (post.isPoll) {
                        // Dynamic poll options results tracker
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            post.pollOptions.forEachIndexed { idx, opt ->
                                val votePercent = post.pollVotes.getOrNull(idx) ?: 50
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { /* trigger select simulation */ },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("$votePercent%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Scientific/Civic value Reactions row (Wise, Helpful, Inspiring, Creative)
                    Text("Reactions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ElevatedAssistChip(
                            onClick = { viewModel.reactToPost(post.id, "wise") },
                            leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, size = 12.dp, tint = Color(0xFFFBBF24)) },
                            label = { Text("Wise  •  ${post.wiseCount}", fontSize = 10.sp) }
                        )

                        ElevatedAssistChip(
                            onClick = { viewModel.reactToPost(post.id, "helpful") },
                            leadingIcon = { Icon(Icons.Default.Handshake, contentDescription = null, size = 12.dp, tint = MaterialTheme.colorScheme.primary) },
                            label = { Text("Helpful  •  ${post.helpfulCount}", fontSize = 10.sp) }
                        )

                        ElevatedAssistChip(
                            onClick = { viewModel.reactToPost(post.id, "inspiring") },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, size = 12.dp, tint = Color(0xFFEF4444)) },
                            label = { Text("Inspiring  •  ${post.inspiringCount}", fontSize = 10.sp) }
                        )

                        ElevatedAssistChip(
                            onClick = { viewModel.reactToPost(post.id, "creative") },
                            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, size = 12.dp, tint = Color(0xFF10B981)) },
                            label = { Text("Creative  •  ${post.creativeCount}", fontSize = 10.sp) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(onClick = {}) {
                            Icon(Icons.Default.Comment, contentDescription = null, size = 12.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${post.commentsCount} Comments", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 10: PUBLIC SQUARE
// ============================================
@Composable
fun SquareTabView(viewModel: MainViewModel) {
    var selectedTabIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = selectedTabIdx, edgePadding = 0.dp) {
            Tab(selected = selectedTabIdx == 0, onClick = { selectedTabIdx = 0 }) {
                Text("Articles", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTabIdx == 1, onClick = { selectedTabIdx = 1 }) {
                Text("Polls", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTabIdx == 2, onClick = { selectedTabIdx = 2 }) {
                Text("Debates", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTabIdx == 3, onClick = { selectedTabIdx = 3 }) {
                Text("Projects", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedTabIdx == 0) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(colors = CardDefaults.cardColors(containerColor = GoldAccent)) {
                                Text("Featured", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = TextOnGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("The Economics of Open Knowledge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("An exploration of why merit-based credit systems outperform attention economies, securing knowledge propagation.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("by Elena R. 🇪🇸", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("+128 KC", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (selectedTabIdx == 2) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Civic Debate: Universal Basic Knowledge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Should every Territory guarantee free access to higher level Arena training resources?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("214 active arguments", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(onClick = {}, shape = RoundedCornerShape(8.dp)) {
                                    Text("Join Debate")
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, size = 48.dp, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No active civic campaigns.", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Initiate a post below to kickstart community topics.", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 11: KNOWLEDGE ARENA (QUIZ PANEL)
// ============================================
@Composable
fun ArenaTabView(viewModel: MainViewModel) {
    val quizIndex by viewModel.currentQuizIndex.collectAsState()
    val isAnswerSelected by viewModel.selectedAnswerIndex.collectAsState()
    val isCorrectState by viewModel.isAnswerCorrect.collectAsState()
    val quizFinished by viewModel.quizCompleted.collectAsState()

    val currentQuestion = viewModel.quizQuestions[quizIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Knowledge Arena", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Participate to earn personal & territory KC.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
            }
        }

        if (!quizFinished) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Q: ${quizIndex + 1} / ${viewModel.quizQuestions.size}") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Award: +15 KC") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = GoldAccent.copy(alpha = 0.2f))
                        )
                    }

                    Text(
                        currentQuestion.question,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentQuestion.options.forEachIndexed { idx, opt ->
                            val isThisSelected = isAnswerSelected == idx
                            val cardBg = when {
                                isThisSelected && isCorrectState == true -> SuccessGreen.copy(alpha = 0.25f)
                                isThisSelected && isCorrectState == false -> DangerRed.copy(alpha = 0.25f)
                                idx == currentQuestion.correctAnswerIndex && isAnswerSelected != null -> SuccessGreen.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                onClick = { viewModel.selectQuizAnswer(idx) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isThisSelected) 1.5.dp else 0.dp,
                                        color = if (isThisSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(opt, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    if (isThisSelected) {
                                        Icon(
                                            imageVector = if (isCorrectState == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (isCorrectState == true) SuccessGreen else DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isAnswerSelected != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (isCorrectState == true) "Correct Assessment!" else "Incorrect Option",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrectState == true) SuccessGreen else DangerRed,
                                fontSize = 13.sp
                            )
                            Text(
                                currentQuestion.explanation,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Button(
                                onClick = { viewModel.nextQuizQuestion() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Continue")
                            }
                        }
                    }
                }
            }
        } else {
            // Quiz completed view
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(60.dp))
                    Text("Arena Cycle Finished", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Your scientific ratings and profile KC credits have compiled successfully. Return tomorrow to participate in the general astronomy loop.", textAlign = TextAlign.Center, fontSize = 13.sp)

                    Button(onClick = { viewModel.resetQuiz() }) {
                        Text("Restart Challenges")
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 13: MESSAGING — THREE-CONNECTION LAYOUT
// ============================================
@Composable
fun MessagesTabView(viewModel: MainViewModel) {
    val connections by viewModel.connections.collectAsState()
    val queued by viewModel.queuedChatRequests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Active Slots (Limit: 3/3)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Encouraging deep conversations over fragmented notifications.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(connections) { chat ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(chat.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase(), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(chat.flag, fontSize = 14.sp)
                                }
                                Text(chat.lastMessage, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(onClick = { viewModel.archiveChat(chat.id) }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive slots", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Divider()

        Column {
            Text("Incoming Queue (${queued.size} pending)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
            Text("Archive active chats above to process incoming consultations.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(queued) { chat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blur(3.dp), // Blurred queue visual constraint
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("??", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("New contact request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Waiting for free session slot", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 14 / 15: EMPIRE & ELECTIONS PANEL
// ============================================
@Composable
fun EmpireTabView(viewModel: MainViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val candidates by viewModel.candidates.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Local territory dashboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(userStats?.flagEmoji ?: "🇮🇳", fontSize = 32.sp)
                            Column {
                                Text("Territory of ${userStats?.territory ?: "India"}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Population: 4.2M registered citizens", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = GoldAccent)) {
                            Text(" Rank #3 ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold, color = TextOnGold, fontSize = 11.sp)
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Regional Governor", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Priya Nair 🇮🇳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Council & Palace View
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.2.dp, GoldAccent, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldAccent)
                        Text("Royal Palace", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(GoldAccent), contentAlignment = Alignment.Center) {
                            Text("QM", fontWeight = FontWeight.Bold, color = TextOnGold)
                        }
                        Column {
                            Text("Queen Maya I", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Reigning since 2025  •  96% Legacy standing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item {
            Text("General Election candidates", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Merit weighted leadership rating index (70% Credits, 30% Votes)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Candidates items
        items(candidates) { cand ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cand.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(cand.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cand.rank, fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text(
                                " Rating: ${String.format("%.1f", cand.leadershipScore)} ",
                                modifier = Modifier.padding(4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        cand.manifesto,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Votes: ${cand.votes}", fontSize = 12.sp)

                        Button(
                            onClick = { viewModel.castElectionVote(cand.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = TextOnGold)
                        ) {
                            Icon(Icons.Default.Ballot, contentDescription = null, size = 12.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cast Vote", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 12: CITIZEN PROFILE TAB
// ============================================
@Composable
fun ProfileTabView(viewModel: MainViewModel) {
    val userStats by viewModel.userStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Linear/radial gradient cover banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Avatar circular overlap
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userStats?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.uppercase() ?: "AS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { viewModel.setShowingSettings(true) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, size = 12.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Modify", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    userStats?.name ?: "Aryan Sharma",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = GoldAccent)) {
                    Text(" ${userStats?.rank ?: "Citizen"} ", modifier = Modifier.padding(2.dp), color = TextOnGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Text(
                "@${userStats?.username ?: "aryan.s"}  •  Territory of ${userStats?.territory ?: "India"} ${userStats?.flagEmoji}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                userStats?.bio ?: "Dedicated citizen of the digital civilization.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics grids cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Knowledge KC", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userStats?.knowledgeCredits ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Contrib CC", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userStats?.contributionCredits ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Reputation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userStats?.reputation ?: 98}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InfoBlue)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Legacy Rating", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userStats?.legacyScore ?: 10}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rank Progression to Noble", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("82%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(
                    progress = { 0.82f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = GoldAccent,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Selected Trait badges", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val traits = userStats?.personalityTraits?.split(",") ?: emptyList()
                traits.forEach { trait ->
                    AssistChip(
                        onClick = {},
                        label = { Text(trait, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Visitors List
            Text("Profile Visitors (open ledger transparencies)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇯🇵", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yumi Tanaka", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = GoldAccent)) {
                                Text(" Baroness ", fontSize = 8.sp, color = TextOnGold, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("2m ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇪🇸", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elena Reyes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = GoldAccent)) {
                                Text(" Noble ", fontSize = 8.sp, color = TextOnGold, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("1h ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 16: NOTIFICATION CENTER OVERLAY
// ============================================
@Composable
fun NotificationCenterSheet(viewModel: MainViewModel) {
    val itemsList by viewModel.notifications.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.setShowingNotificationCenter(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .imePadding()
                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notification Centre",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.setShowingNotificationCenter(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.markNotificationsRead() }) {
                            Text("Mark all read", fontSize = 12.sp)
                        }
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text("Clear all", fontSize = 12.sp, color = DangerRed)
                        }
                    }

                    Divider()

                    Spacer(modifier = Modifier.height(8.dp))

                    if (itemsList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, size = 48.dp, tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your ledger record has no pending alerts.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(itemsList) { item ->
                                val bgAlert = if (!item.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = bgAlert),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (!item.isRead) 1.dp else 0.dp,
                                            color = if (!item.isRead) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = when (item.type) {
                                                "VISITOR" -> Icons.Default.Visibility
                                                "PROMOTION" -> Icons.Default.WorkspacePremium
                                                "REACTION" -> Icons.Default.Lightbulb
                                                "MESSAGE" -> Icons.Default.Mail
                                                "ELECTION" -> Icons.Default.Ballot
                                                else -> Icons.Default.Public
                                            },
                                            contentDescription = null,
                                            tint = if (!item.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(item.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteNotification(item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove notify", size = 14.dp, tint = DangerRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive mock actions generator
                Button(
                    onClick = { viewModel.createMockNotification() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Mock Event Notification")
                }
            }
        }
    }
}

// ============================================
// SCREEN 09: COMPOSE NEW RECORD
// ============================================
@Composable
fun CreatePostDialog(viewModel: MainViewModel) {
    var contentText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Project") }

    val postTypes = listOf("Project", "Civic Poll", "Article", "Question")

    Dialog(
        onDismissRequest = { viewModel.setShowingCreatePost(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .imePadding()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Broadcast Civic Proposal", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    placeholder = { Text("What civic knowledge or system study do you declare today, Citizen?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Campaign category style", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    postTypes.forEach { type ->
                        val active = type == selectedType
                        FilterChip(
                            selected = active,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.setShowingCreatePost(false) }) {
                        Text("Cancel", color = DangerRed)
                    }
                    Button(
                        onClick = { viewModel.publishPost(contentText, selectedType) },
                        enabled = contentText.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish")
                    }
                }
            }
        }
    }
}

// ============================================
// SCREEN 17: SETTINGS / THEME SELECTOR SHEET
// ============================================
@Composable
fun AppearanceSettingsSheet(viewModel: MainViewModel) {
    val activeChoice by viewModel.appearanceTheme.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.setShowingSettings(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .imePadding()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Identity Control (Settings)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { viewModel.setShowingSettings(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close settings")
                    }
                }

                Text("Theme Style Preferences", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                // 3-way segmented appearance choice selector
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Light", "Dark", "System").forEach { label ->
                        val isSel = label == activeChoice
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.updateTheme(label) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = when (label) {
                                        "Light" -> Icons.Default.LightMode
                                        "Dark" -> Icons.Default.DarkMode
                                        else -> Icons.Default.SettingsSuggest
                                    },
                                    contentDescription = null,
                                    size = 14.dp,
                                    tint = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    "Primary color standard is Royal Blue on White (system default). Selecting 'Dark' shifts surfaces into Obsidian deep blue while conserving royal multipliers.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                val backendUrl by viewModel.backendUrl.collectAsState()
                var tempUrl by remember(backendUrl) { mutableStateOf(backendUrl) }

                Text("Backend Link Server Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("Server Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.updateBackendUrl(tempUrl) }) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync dynamic connection")
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.updateBackendUrl(tempUrl) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect & Sync")
                    }
                    OutlinedButton(
                        onClick = {
                            tempUrl = "http://10.0.2.2:8080"
                            viewModel.updateBackendUrl(tempUrl)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Local Loopback")
                    }
                }

                Divider()

                Text("Citizen Standing Stats", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Digital Verified Card", fontSize = 12.sp)
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Passport Geography", fontSize = 12.sp)
                    Text("Territory of ${userStats?.territory} ${userStats?.flagEmoji}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.setShowingExitSummary(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Initiate Session Logout")
                }
            }
        }
    }
}

// ============================================
// SCREEN 18: LOGOUT SUMMARY
// ============================================
@Composable
fun ExitSummaryDialog(viewModel: MainViewModel) {
    val statsSessionKC by viewModel.sessionKcEarned.collectAsState()
    val statsSessionCC by viewModel.sessionCcEarned.collectAsState()
    val visitorsSession by viewModel.sessionVisitorsToday.collectAsState()
    val statsUserDoc by viewModel.userStats.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.setShowingExitSummary(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .imePadding()
                .border(2.dp, GoldAccent, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
                Text("Today's Session Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Knowledge compiled", fontSize = 12.sp)
                            Text("+$statsSessionKC KC", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Social contributions certified", fontSize = 12.sp)
                            Text("+$statsSessionCC CC", fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Profile visitors caught", fontSize = 12.sp)
                            Text("$visitorsSession visitors", fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Regional Territory status", fontSize = 12.sp)
                            Text("India raised ▲", fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                    }
                }

                Text(
                    "\"The Empire awaits your return, Citizen.\"",
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setShowingExitSummary(false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Return")
                    }
                    Button(
                        onClick = { viewModel.executeLogout() },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Complete Logout")
                    }
                }
            }
        }
    }
}

// Helpers
@Composable
fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color = Color.Unspecified) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size)
    )
}
