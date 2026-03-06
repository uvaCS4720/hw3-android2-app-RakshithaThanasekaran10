package edu.nd.pmcburne.hwapp.one.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Retrofit interface for the NCAA scoreboard API
interface ApiService {
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScoreboard(
        // fetches the scoreboard for a given gender, division, and date
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ScoreboardResponse
}

// top level API response containing a list of game wrappers
data class ScoreboardResponse(
    @SerializedName("games") val games: List<GameWrapper>? = emptyList()
)

// wrapper object around each game in the API response, the API stores each game under a "game" key within the array
data class GameWrapper(
    @SerializedName("game") val game: Game? = null
)

// represents a single basketball game from the API
// contains team data, game state, timing info, and metadata
data class Game(
    @SerializedName("gameID") val gameID: String,
    @SerializedName("home") val home: TeamData? = null,
    @SerializedName("away") val away: TeamData? = null,
    @SerializedName("gameState") val gameState: String? = "pre",  // "pre", "live", "final"
    @SerializedName("finalMessage") val finalMessage: String? = "",
    @SerializedName("startTime") val startTime: String? = "",
    @SerializedName("startDate") val startDate: String? = "",
    @SerializedName("currentPeriod") val currentPeriod: String? = "",
    @SerializedName("contestClock") val contestClock: String? = "0:00",
    @SerializedName("title") val title: String? = ""
)

// represents one team's data within a game (either home or away)
data class TeamData(
    @SerializedName("score") val score: String? = "",
    @SerializedName("winner") val winner: Boolean? = false,
    @SerializedName("names") val names: TeamNames? = null
)

// contains the various name formats for a team
data class TeamNames(
    @SerializedName("short") val short: String? = "",
    @SerializedName("full") val full: String? = ""
)

// singleton that provides an instance of the ApiService
// uses Gson for JSON conversion
object RetrofitInstance {
    private const val BASE_URL = "https://ncaa-api.henrygd.me/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}