package edu.nd.pmcburne.hwapp.one.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database entity representing a single basketball game
// each row in the "games" table corresponds to one game on a specific date
// games are uniquely identified by their API-provided gameID
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val gender: String,
    val date: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: String,
    val awayScore: String,
    val statusName: String,
    val statusDescription: String,
    val displayClock: String,
    val period: Int,
    val startTime: String,
    val homeWinner: Boolean,
    val awayWinner: Boolean,
    val completed: Boolean
)