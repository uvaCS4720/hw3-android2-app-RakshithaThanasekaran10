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

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    private val _games = MutableStateFlow<List<GameEntity>>(emptyList())
    val games: StateFlow<List<GameEntity>> = _games.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isMens = MutableStateFlow(true)
    val isMens: StateFlow<Boolean> = _isMens.asStateFlow()

    private val _selectedDate = MutableStateFlow(todayTriple())
    val selectedDate: StateFlow<Triple<Int, Int, Int>> = _selectedDate.asStateFlow()

    init { loadGames() }

    fun setDate(year: Int, month: Int, day: Int) {
        _selectedDate.value = Triple(year, month, day)
        loadGames()
    }

    fun setGender(mens: Boolean) {
        _isMens.value = mens
        loadGames()
    }

    fun refresh() { loadGames() }

    private fun loadGames() {
        val (year, month, day) = _selectedDate.value
        val gender = if (_isMens.value) "men" else "women"
        val dateStr = "$year-${month.toString().padStart(2,'0')}-${day.toString().padStart(2,'0')}"

        viewModelScope.launch {
            repository.getGames(gender, dateStr).collectLatest { dbGames ->
                _games.value = dbGames
            }
        }

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

    private fun todayTriple(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}