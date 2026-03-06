package edu.nd.pmcburne.hwapp.one.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScoreboard(
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ScoreboardResponse
}

data class ScoreboardResponse(
    @SerializedName("games") val games: List<GameWrapper>? = emptyList()
)

data class GameWrapper(
    @SerializedName("game") val game: Game? = null
)

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

data class TeamData(
    @SerializedName("score") val score: String? = "",
    @SerializedName("winner") val winner: Boolean? = false,
    @SerializedName("names") val names: TeamNames? = null
)

data class TeamNames(
    @SerializedName("short") val short: String? = "",
    @SerializedName("full") val full: String? = ""
)

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