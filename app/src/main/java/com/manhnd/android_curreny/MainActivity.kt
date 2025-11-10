package com.manhnd.android_curreny

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.manhnd.android_curreny.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // USD: US Dollar
    // EUR: Euro
    // VND: Viet Nam Dong
    // JPY: Yen
    // CNY: China
    // KRW: Korean won
    private val currencies = listOf(
        "USD", "EUR", "VND", "JPY",  "CNY", "KRW",
    )

    private val ratesToUsd = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "VND" to 25350.0,
        "JPY" to 150.0,
        "CNY" to 7.18,
        "KRW" to 1345.0,
    )

    private val decimalFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

    private var updatingFromField = false
    private var updatingToField = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()

        binding.fromAmountEditText.setText("1")
        updateToAmount()
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )
        binding.fromCurrencySpinner.adapter = adapter
        binding.toCurrencySpinner.adapter = adapter
        binding.toCurrencySpinner.setSelection(1)
    }

    private fun setupListeners() {
        binding.fromCurrencySpinner.onSelectionChanged { updateToAmount() }
        binding.toCurrencySpinner.onSelectionChanged { updateToAmount() }

        binding.fromAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (updatingFromField) return
                updatingToField = true
                updateToAmount()
                updatingToField = false
            }
        })

        binding.toAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (updatingToField) return
                updatingFromField = true
                updateFromAmount()
                updatingFromField = false
            }
        })
    }

    private fun updateToAmount() {
        val amount = parseAmount(binding.fromAmountEditText)
        val fromCurrency = binding.fromCurrencySpinner.selectedItem as String
        val toCurrency = binding.toCurrencySpinner.selectedItem as String
        val converted = convert(amount, fromCurrency, toCurrency)
        val formatted = decimalFormat.format(converted)
        if (binding.toAmountEditText.text.toString() != formatted) {
            updatingToField = true
            binding.toAmountEditText.setText(formatted)
            binding.toAmountEditText.setSelection(formatted.length)
            updatingToField = false
        }
    }

    private fun updateFromAmount() {
        val amount = parseAmount(binding.toAmountEditText)
        val fromCurrency = binding.fromCurrencySpinner.selectedItem as String
        val toCurrency = binding.toCurrencySpinner.selectedItem as String
        val converted = convert(amount, toCurrency, fromCurrency)
        val formatted = decimalFormat.format(converted)
        if (binding.fromAmountEditText.text.toString() != formatted) {
            updatingFromField = true
            binding.fromAmountEditText.setText(formatted)
            binding.fromAmountEditText.setSelection(formatted.length)
            updatingFromField = false
        }
    }

    private fun parseAmount(editText: EditText): Double {
        val raw = editText.text.toString()
        if (raw.isBlank()) return 0.0
        val normalized = raw.replace(",", "").replace(" ", "")
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val fromRate = ratesToUsd[fromCurrency] ?: error("Unknown currency: $fromCurrency")
        val toRate = ratesToUsd[toCurrency] ?: error("Unknown currency: $toCurrency")
        if (fromRate == 0.0) return 0.0
        val usdAmount = amount / fromRate
        return usdAmount * toRate
    }

    private fun Spinner.onSelectionChanged(onSelect: () -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                onSelect()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }
}
