package se.andynet.simplecoffeecalc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val ratioIndex: Int = 0,
    val isWater: Boolean = true,
    val weightText: String = "",
    val resultShort: String = "",
    val resultLong: String = ""
)

// Holds all screen state in memory, so rotation no longer round-trips through disk.
// Persistence to DataStore only happens explicitly, via persist().
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.readSettings()
            _uiState.update { current ->
                val ratioValues = ratioValues()
                val restoredRatioIndex = settings.ratio
                    ?.let { ratio -> ratioValues.indexOf(ratio.toString()).takeIf { it >= 0 } }
                    ?: current.ratioIndex

                recalculate(
                    current.copy(
                        ratioIndex = restoredRatioIndex,
                        isWater = settings.isWater ?: current.isWater,
                        weightText = settings.weight?.toString() ?: current.weightText
                    )
                )
            }
        }
    }

    fun onRatioIndexChanged(index: Int) {
        _uiState.update { recalculate(it.copy(ratioIndex = index)) }
    }

    fun onIsWaterChanged(isWater: Boolean) {
        _uiState.update { recalculate(it.copy(isWater = isWater)) }
    }

    fun onWeightTextChanged(text: String) {
        _uiState.update { recalculate(it.copy(weightText = text)) }
    }

    // Launched on the Application's own scope (not viewModelScope/lifecycleScope) so the
    // write always runs to completion, even if this ViewModel or its Activity gets torn
    // down moments later.
    fun persist() {
        val state = _uiState.value
        val ratioValue = ratioValues().getOrNull(state.ratioIndex)?.toIntOrNull()
        val weight = state.weightText.toIntOrNull() ?: 0

        (getApplication<Application>() as CoffeeApp).applicationScope.launch {
            repository.saveSettings(Settings(ratioValue, state.isWater, weight))
        }
    }

    private fun ratioValues(): Array<String> =
        getApplication<Application>().resources.getStringArray(R.array.ratioValues)

    private fun recalculate(state: CalculatorUiState): CalculatorUiState {
        val ratioSelected = ratioValues().getOrNull(state.ratioIndex)?.toIntOrNull()
        val measuredWeight = state.weightText.toIntOrNull()

        if (ratioSelected == null || measuredWeight == null) {
            return state.copy(resultShort = "", resultLong = "")
        }

        return if (state.isWater) {
            val amountOfBeans = (measuredWeight.toDouble() / ratioSelected.toDouble()).toInt()
            state.copy(
                resultShort = "$amountOfBeans grams",
                resultLong = "For $measuredWeight grams of water you need \n$amountOfBeans grams of coffee"
            )
        } else {
            val amountOfWater = (measuredWeight.toDouble() * ratioSelected.toDouble()).toInt()
            state.copy(
                resultShort = "$amountOfWater grams",
                resultLong = "For $measuredWeight grams of coffee you need \n$amountOfWater grams of water"
            )
        }
    }
}
