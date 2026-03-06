package edu.nd.pmcburne.hwapp.one.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import edu.nd.pmcburne.hwapp.one.data.api.Game
import edu.nd.pmcburne.hwapp.one.data.api.RetrofitInstance
import edu.nd.pmcburne.hwapp.one.data.db.AppDatabase
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import kotlinx.coroutines.flow.Flow

// repository that coordinates data access between the API and the local database
class GameRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).gameDao()

    // returns a flow of games from the local database for the given gender and date
    fun getGames(gender: String, date: String): Flow<List<GameEntity>> =
        dao.getGames(gender, date)

    // checks whether the device currently has an active internet connection
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // fetches the latest scoreboard data from the API and stores it in the local database
    suspend fun fetchAndStore(gender: String, year: String, month: String, day: String) {
        val date = "$year-$month-$day"
        val response = RetrofitInstance.api.getScoreboard(gender, year, month, day)
        val games = response.games ?: return
        val entities = games.mapNotNull { it.game?.toEntity(gender, date) }
        dao.upsertGames(entities)
    }

    // extension function to map a Game object to a GameEntity
    private fun Game.toEntity(gender: String, date: String): GameEntity {
        val isLive = gameState == "live"
        val isFinal = gameState == "final"

        // Map period string like "1st", "2nd" to int
        val periodInt = when (currentPeriod?.lowercase()) {
            "1st" -> 1
            "2nd" -> 2
            "3rd" -> 3
            "4th" -> 4
            "ot" -> 5
            else -> 0
        }

        val statusName = when {
            isFinal -> "STATUS_FINAL"
            isLive -> "STATUS_IN_PROGRESS"
            else -> "STATUS_SCHEDULED"
        }

        return GameEntity(
            id = gameID,
            gender = gender,
            date = date,
            homeTeam = home?.names?.short ?: "TBD",
            awayTeam = away?.names?.short ?: "TBD",
            homeScore = home?.score ?: "0",
            awayScore = away?.score ?: "0",
            statusName = statusName,
            statusDescription = when {
                isFinal -> "Final"
                isLive -> "In Progress"
                else -> "Scheduled"
            },
            displayClock = contestClock ?: "0:00",
            period = periodInt,
            startTime = startTime ?: "",
            homeWinner = home?.winner ?: false,
            awayWinner = away?.winner ?: false,
            completed = isFinal
        )
    }
}