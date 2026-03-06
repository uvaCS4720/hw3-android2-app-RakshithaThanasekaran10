package edu.nd.pmcburne.hwapp.one.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// main Room database for the app
// contains a single table for games
@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // provides access to game-related database operations via GameDao
    abstract fun gameDao(): GameDao

    companion object {
        // volatile ensures that the INSTANCE variable is always up-to-date and visible to all threads
        @Volatile private var INSTANCE: AppDatabase? = null

        // returns a singleton instance of the database
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "basketball_scores.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}