package edu.nd.pmcburne.hwapp.one.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import edu.nd.pmcburne.hwapp.one.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.Job

// ViewModel for the main screen. survives configuration changes
// holds the selected date, gender filter, game list, loading state, and error message as StateFlows that the UI observes and reacts to automatically
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    // current list of games to display, sources from local Room database
    private val _games = MutableStateFlow<List<GameEntity>>(emptyList())
    val games: StateFlow<List<GameEntity>> = _games.asStateFlow()

    // true while an API fetch is in progress
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // true if the device currently has an active internet connection
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // holds an error message if the API fetch fails, null otherwise
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // true if showing men's games, false for women's
    private val _isMens = MutableStateFlow(true)
    val isMens: StateFlow<Boolean> = _isMens.asStateFlow()

    // currently selected date as a triple, defaults to today
    private val _selectedDate = MutableStateFlow(todayTriple())
    val selectedDate: StateFlow<Triple<Int, Int, Int>> = _selectedDate.asStateFlow()

    // trigger initial load when the ViewModel is first created
    init { loadGames() }

    // updates the selected date and reloads games for the new date
    fun setDate(year: Int, month: Int, day: Int) {
        _selectedDate.value = Triple(year, month, day)
        loadGames()
    }

    // switches between men's and women's games and reloads
    fun setGender(mens: Boolean) {
        _isMens.value = mens
        loadGames()
    }

    // manually triggers a reload, used by the refresh button and pull to refresh
    fun refresh() { loadGames() }

    private var collectJob: Job? = null

    private fun loadGames() {
        val (year, month, day) = _selectedDate.value
        val gender = if (_isMens.value) "men" else "women"
        val dateStr = "$year-${month.toString().padStart(2,'0')}-${day.toString().padStart(2,'0')}"

        // Cancels the previous database observer before starting a new one for the new date/gender
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            repository.getGames(gender, dateStr).collectLatest { dbGames ->
                _games.value = dbGames
            }
        }

        // fetches fresh data from the API if online, then store it in the database
        // the Flow collector above will automatically update the UI when the database changes
        viewModelScope.launch {
            _isOnline.value = repository.isOnline()
            if (repository.isOnline()) {
                _isLoading.value = true
                _errorMessage.value = null
                try {
                    repository.fetchAndStore(
                        gender,
                        year.toString(),
                        month.toString().padStart(2, '0'),
                        day.toString().padStart(2, '0')
                    )
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to refresh: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    // returns today's date as a (year, month, day) triple.
    private fun todayTriple(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}