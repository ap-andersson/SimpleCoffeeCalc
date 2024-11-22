package se.andynet.simplecoffeecalc

import android.content.Context
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

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
    }

    fun calculate() {
        val waterOrBeansSpinner: Spinner = findViewById(R.id.waterOrBeansDropdown)
        val ratioSpinner: Spinner = findViewById(R.id.ratioDropdown)
        val gramsInputNumber: EditText = findViewById(R.id.gramsInputNumber)
        val resultTextLong: TextView = findViewById(R.id.resultTextLong)
        val resultTextShort: TextView = findViewById(R.id.resultTextShort)

        val ratioSelected = resources.getStringArray(R.array.ratioValues)[ratioSpinner.selectedItemPosition].toInt().toDouble()
        val measuredWeight = gramsInputNumber.text.toString().toIntOrNull()

        if (measuredWeight == null) {
            resultTextShort.text = ""
            resultTextLong.text = ""
        } else {
            val isWater = waterOrBeansSpinner.selectedItemPosition == 0

            if (isWater) {
                var amountOfBeans: Int = (measuredWeight.toDouble() * (1/ratioSelected)).toInt()
                resultTextShort.text = "$amountOfBeans grams"
                resultTextLong.text = "For $measuredWeight grams of water you need \n$amountOfBeans grams of beans"
            } else {
                var amountOfWater: Int = (measuredWeight.toDouble() * ratioSelected).toInt()
                resultTextShort.text = "$amountOfWater grams"
                resultTextLong.text = "For $measuredWeight grams of beans you need \n$amountOfWater grams of water"
            }
        }
    }
}