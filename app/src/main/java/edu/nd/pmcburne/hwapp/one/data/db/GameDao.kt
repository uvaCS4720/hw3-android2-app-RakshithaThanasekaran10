package edu.nd.pmcburne.hwapp.one.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// data access object for the games table
// provides methods to insert, query, and delete games from the local room database
@Dao
interface GameDao {
    // inserts or updates a list of games in the database
    // REPLACE strategy makes sure that if a game with same ID already exists, it is replaced
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGames(games: List<GameEntity>)

    // returns a live flow of all games for a given gender and date
    // flow will emit a new list of games whenever the database is updated
    @Query("SELECT * FROM games WHERE gender = :gender AND date = :date ORDER BY startTime ASC")
    fun getGames(gender: String, date: String): Flow<List<GameEntity>>

    // deletes all games for a given gender and date
    @Query("DELETE FROM games WHERE gender = :gender AND date = :date")
    suspend fun deleteGames(gender: String, date: String)
}