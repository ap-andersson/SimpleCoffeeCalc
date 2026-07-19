package se.andynet.simplecoffeecalc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import se.andynet.simplecoffeecalc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // Guards against the programmatic view updates in render() re-triggering the
    // listeners below and feeding a stale value back into the ViewModel.
    private var isApplyingState = false

    override fun onPause() {
        viewModel.persist()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.ratioNames,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.ratioDropdown.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.waterBeansOption,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.waterOrBeansDropdown.adapter = adapter
        }

        binding.gramsInputNumber.doAfterTextChanged { text ->
            if (isApplyingState) return@doAfterTextChanged
            viewModel.onWeightTextChanged(text?.toString().orEmpty())
        }

        binding.ratioDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isApplyingState) return
                viewModel.onRatioIndexChanged(position)
            }
        }

        binding.waterOrBeansDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isApplyingState) return
                viewModel.onIsWaterChanged(position == 0)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: CalculatorUiState) {
        isApplyingState = true

        if (binding.ratioDropdown.selectedItemPosition != state.ratioIndex) {
            binding.ratioDropdown.setSelection(state.ratioIndex)
        }

        val waterPosition = if (state.isWater) 0 else 1
        if (binding.waterOrBeansDropdown.selectedItemPosition != waterPosition) {
            binding.waterOrBeansDropdown.setSelection(waterPosition)
        }

        if (binding.gramsInputNumber.text?.toString() != state.weightText) {
            binding.gramsInputNumber.setText(state.weightText)
            binding.gramsInputNumber.setSelection(binding.gramsInputNumber.text?.length ?: 0)
        }

        isApplyingState = false

        binding.resultTextShort.text = state.resultShort
        binding.resultTextLong.text = state.resultLong
    }
}
