package edu.nd.pmcburne.hwapp.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import edu.nd.pmcburne.hwapp.one.ui.viewmodel.MainViewModel
import java.util.Calendar

//main screen composable, displays the date picker, controls, and game list
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    // collects all UI state from the ViewModel as compose state
    val games by vm.games.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val isOnline by vm.isOnline.collectAsState()
    val isMens by vm.isMens.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    //controls visibility of the date picker
    var showDatePicker by remember { mutableStateOf(false) }

    // pull to refresh state, tied to the loading indicator and refresh action
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { vm.refresh() }
    )

    val (year, month, day) = selectedDate
    val dateLabel = "%04d-%02d-%02d".format(year, month, day)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("NCAA Basketball", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // shows a wifi off icon in the toolbar when the device is offline
                    if (!isOnline) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = "Offline",
                            tint = Color.Yellow,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    // manual refresh button
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Controls
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(dateLabel, fontWeight = FontWeight.Medium)
                        }

                        // men's/women's toggle using FilterChips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = isMens,
                                onClick = { vm.setGender(true) },
                                label = { Text("Men's") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !isMens,
                                onClick = { vm.setGender(false) },
                                label = { Text("Women's") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Offline banner
                if (!isOnline) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3CD))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Offline — showing cached scores", color = Color(0xFF856404), fontSize = 13.sp)
                    }
                }

                // Error message
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }

                // Loading bar
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Game list
                if (!isLoading && games.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No games found for this date.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games, key = { it.id }) { game ->
                            GameCard(game = game, isMens = isMens)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = run {
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, day, 12, 0, 0)
                cal.timeInMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        vm.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// displays a single game as a card

@Composable
fun GameCard(game: GameEntity, isMens: Boolean) {
    val isInProgress = game.statusName.contains("IN_PROGRESS", ignoreCase = true)
    val isFinal = game.completed || game.statusName.contains("FINAL", ignoreCase = true)
    val isScheduled = !isInProgress && !isFinal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInProgress) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(isInProgress = isInProgress, isFinal = isFinal, game = game, isMens = isMens)
                if (isInProgress) {
                    Text(
                        "LIVE",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x22FF0000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            TeamRow(teamName = game.awayTeam, score = game.awayScore, isWinner = game.awayWinner, showScore = !isScheduled)
            Spacer(Modifier.height(6.dp))
            TeamRow(teamName = game.homeTeam, score = game.homeScore, isWinner = game.homeWinner, showScore = !isScheduled, isHome = true)

            if (isScheduled) {
                Spacer(Modifier.height(8.dp))
                Text("${game.startTime}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// colored badge showing the current game status
@Composable
fun StatusBadge(isInProgress: Boolean, isFinal: Boolean, game: GameEntity, isMens: Boolean) {
    val text = when {
        isFinal -> "Final"
        isInProgress -> {
            val periodLabel = if (isMens) {
                when (game.period) { 1 -> "1st Half"; 2 -> "2nd Half"; else -> "OT" }
            } else {
                when (game.period) { 1 -> "1st Qtr"; 2 -> "2nd Qtr"; 3 -> "3rd Qtr"; 4 -> "4th Qtr"; else -> "OT" }
            }
            "$periodLabel · ${game.displayClock}"
        }
        else -> "Upcoming"
    }
    val bgColor = when { isFinal -> Color(0xFFE8F5E9); isInProgress -> Color(0xFFE3F2FD); else -> Color(0xFFF5F5F5) }
    val textColor = when { isFinal -> Color(0xFF2E7D32); isInProgress -> Color(0xFF1565C0); else -> Color(0xFF616161) }

    Text(
        text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// displays a single team row with an optional home indicator, team name, and score
@Composable
fun TeamRow(teamName: String, score: String, isWinner: Boolean, showScore: Boolean, isHome: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isHome) {
                Text("H", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 1.dp))
                Spacer(Modifier.width(6.dp))
            } else {
                Spacer(Modifier.width(22.dp))
            }
            Text(
                teamName,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp,
                color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (isWinner) {
                Spacer(Modifier.width(4.dp))
                Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
        if (showScore) {
            Text(
                score,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                fontSize = 18.sp,
                color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}