package edu.nd.pmcburne.hwapp.one.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGames(games: List<GameEntity>)

    @Query("SELECT * FROM games WHERE gender = :gender AND date = :date ORDER BY startTime ASC")
    fun getGames(gender: String, date: String): Flow<List<GameEntity>>

    @Query("DELETE FROM games WHERE gender = :gender AND date = :date")
    suspend fun deleteGames(gender: String, date: String)
}