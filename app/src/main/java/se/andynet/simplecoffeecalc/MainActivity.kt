package se.andynet.simplecoffeecalc

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val USER_PREFERENCES_NAME = "datastore_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

private val ratioKey = intPreferencesKey("ratio")
private val isWaterKey = booleanPreferencesKey("isWater")
private val weightKey = intPreferencesKey("weight")

class MainActivity : AppCompatActivity() {

    override fun onPause() {

        Log.d("Debugging", "onPause")

        lifecycleScope.launch {
            savePreference()
        }

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        ArrayAdapter.createFromResource(
            this,
            R.array.ratioNames,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ratioSpinner.adapter = adapter
        }

        val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        ArrayAdapter.createFromResource(
            this,
            R.array.waterBeansOption,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            waterOrBeansSpinner.adapter = adapter
        }


        val gramsInputNumber: EditText = findViewById(R.id.gramsInputNumber)
        gramsInputNumber.doAfterTextChanged {
            calculate();
        }

        ratioSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                calculate()
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calculate()
            }
        }

        waterOrBeansSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                calculate()
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calculate()
            }
        }

        lifecycleScope.launch {

            val settings = readPreference()

            if(settings.isWater != null && !settings.isWater) {
                Log.d("Debugging", "Setting waterOrBeansSpinner to 1")
                waterOrBeansSpinner.setSelection(1)
            }

            if(settings.weight != null) {
                Log.d("Debugging", "Setting gramsInputNumber to " + settings.weight.toString())
                gramsInputNumber.setText(settings.weight.toString())
            }

            if(settings.ratio != null) {
                Log.d("Debugging", "Setting ratioSpinner to " + (settings.ratio-15).toString())
                ratioSpinner.setSelection(settings.ratio-15)
            }

        }
    }

    // Writing on the file
    private suspend fun savePreference() {

        val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        val gramsInputNumber: EditText = findViewById(R.id.gramsInputNumber)

        val ratioSelected = resources.getStringArray(R.array.ratioValues)[ratioSpinner.selectedItemPosition].toInt()
        val measuredWeight = gramsInputNumber.text.toString().toIntOrNull()
        val isWater = waterOrBeansSpinner.selectedItemPosition == 0


        var actualWeight = 0;
        if(measuredWeight != null) {
            actualWeight = measuredWeight
        }

        Log.d("Debugging", "Saving preferences. ratioSelected:" + ratioSelected + ". isWater:" + isWater + ". actualWeight:" + actualWeight)

        dataStore.edit { it: MutablePreferences ->
            it[ratioKey] = ratioSelected
            it[isWaterKey] = isWater
            it[weightKey] = actualWeight
        }
    }

    // Reading the file
    private suspend fun readPreference(): Settings {

        val pref = dataStore.data.first()

        Log.d("Debugging", "Reading preferences. pref[ratioKey]:" + pref[ratioKey] + ". pref[isWaterKey]:" + pref[isWaterKey] + ". pref[weightKey]:" + pref[weightKey])

        return Settings(pref[ratioKey], pref[isWaterKey], pref[weightKey])
    }

    fun calculate() {
        val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        val gramsInputNumber: EditText = findViewById(R.id.gramsInputNumber)
        val resultTextLong: TextView = findViewById(R.id.resultTextLong)
        val resultTextShort: TextView = findViewById(R.id.resultTextShort)

        val ratioSelected = resources.getStringArray(R.array.ratioValues)[ratioSpinner.selectedItemPosition].toInt()
        val measuredWeight = gramsInputNumber.text.toString().toIntOrNull()
        val isWater = waterOrBeansSpinner.selectedItemPosition == 0

        if (measuredWeight == null) {
            resultTextShort.text = ""
            resultTextLong.text = ""
        } else {

            if (isWater) {
                var amountOfBeans: Int = (measuredWeight.toDouble() * (1/ratioSelected.toDouble())).toInt()
                resultTextShort.text = "$amountOfBeans grams"
                resultTextLong.text = "For $measuredWeight grams of water you need \n$amountOfBeans grams of coffee"
            } else {
                var amountOfWater: Int = (measuredWeight.toDouble() * ratioSelected.toDouble()).toInt()
                resultTextShort.text = "$amountOfWater grams"
                resultTextLong.text = "For $measuredWeight grams of coffee you need \n$amountOfWater grams of water"
            }
        }
    }
}

data class Settings(val ratio: Int?, val isWater: Boolean?, val weight: Int?)
