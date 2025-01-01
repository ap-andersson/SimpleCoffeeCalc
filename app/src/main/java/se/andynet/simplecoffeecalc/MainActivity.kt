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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "datastore_preferences")
val ratioKey = intPreferencesKey("ratio")
val isWaterKey = booleanPreferencesKey("isWater")
val weightKey = intPreferencesKey("weight")

class MainActivity : AppCompatActivity() {

    override fun onPause() {

        Log.d("Debugging", "onPause")

        lifecycleScope.launch {
            savePreference()
        }

        super.onPause()
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//
//        Log.d("Debugging", "onSaveInstanceState")
//
//        lifecycleScope.launch {
//            savePreference()
//        }
//
//        super.onSaveInstanceState(outState)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        //val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        //val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        //val gramsInputNumber: EditText = findViewById(R.id.gramsInputNumber)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.ratioNames,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            ratioSpinner.adapter = adapter
        }

        val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.waterBeansOption,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
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

        Log.d("Debugging", "Saving preferences")

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

        Log.d("Debugging", "ratioSelected:" + ratioSelected)
        Log.d("Debugging", "isWater:" + isWater)
        Log.d("Debugging", "actualWeight:" + actualWeight)

        dataStore.edit { it: MutablePreferences ->
            Log.d("Debugging", "inside datastore.edit")
            it[ratioKey] = ratioSelected
            it[isWaterKey] = isWater
            it[weightKey] = actualWeight
        }
    }

    // Reading the file
    private suspend fun readPreference(): Settings {

        Log.d("Debugging", "readPreference")

        //val ratioKey = intPreferencesKey("ratio")
        //val isWaterKey = booleanPreferencesKey("isWater")
        //val weightKey = intPreferencesKey("weight")

        val pref = dataStore.data.first()

        Log.d("Debugging", "pref[ratioKey]:"+pref[ratioKey])
        Log.d("Debugging", "pref[isWaterKey]:"+pref[isWaterKey])
        Log.d("Debugging", "pref[weightKey]:"+pref[weightKey])


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
                resultTextLong.text = "For $measuredWeight grams of water you need \n$amountOfBeans grams of beans"
            } else {
                var amountOfWater: Int = (measuredWeight.toDouble() * ratioSelected.toDouble()).toInt()
                resultTextShort.text = "$amountOfWater grams"
                resultTextLong.text = "For $measuredWeight grams of beans you need \n$amountOfWater grams of water"
            }
        }
    }
}

data class Settings(val ratio: Int?, val isWater: Boolean?, val weight: Int?)