package com.cmpe277.bitensip

import android.text.format.DateFormat
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.cmpe277.bitensip.databinding.ActivityInitUserInfoBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class InitUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitUserInfoBinding
    private lateinit var sharedPref: SharedPreferences

    private var wakeupTime: Long = 0
    private var sleepingTime: Long = 0

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        sharedPref = getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)

        wakeupTime = sharedPref.getLong(AppUtils.WAKEUP_TIME, 1558323000000)
        sleepingTime = sharedPref.getLong(AppUtils.SLEEPING_TIME_KEY, 1558369800000)

        setupTimePicker(binding.etWakeUpTime.editText!!, "Select Wakeup Time", wakeupTime) { time ->
            wakeupTime = time
            binding.etWakeUpTime.editText!!.setText(formatTime(time))
        }

        setupTimePicker(binding.etSleepTime.editText!!, "Select Sleeping Time", sleepingTime) { time ->
            sleepingTime = time
            binding.etSleepTime.editText!!.setText(formatTime(time))
        }

        binding.btnContinue.setOnClickListener { view ->
            hideKeyboard()

            val weightInput = binding.etWeight.editText?.text.toString().trim()
            val workTimeInput = binding.etWorkTime.editText?.text.toString().trim()

            when {
                weightInput.isBlank() -> showSnackbar(view, "Please input your weight")
                weightInput.toIntOrNull() !in 20..200 -> showSnackbar(view, "Please input a valid weight between 20 and 200")
                workTimeInput.isBlank() -> showSnackbar(view, "Please input your workout time")
                workTimeInput.toIntOrNull() !in 0..500 -> showSnackbar(view, "Please input a valid workout time between 0 and 500")
                else -> saveUserData(weightInput.toInt(), workTimeInput.toInt())
            }
        }
    }

    private fun setupTimePicker(editText: View, title: String, initialTime: Long, onTimeSet: (Long) -> Unit) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = initialTime }
            val is24hFormat = DateFormat.is24HourFormat(this@InitUserInfoActivity)

            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val newCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }
                    onTimeSet(newCalendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24hFormat
            ).apply {
                setTitle(title)
                show()
            }
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.initUserInfoParentLayout.windowToken, 0)
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun saveUserData(weight: Int, workTime: Int) {
        sharedPref.edit().apply {
            putInt(AppUtils.WEIGHT_KEY, weight)
            putInt(AppUtils.WORK_TIME_KEY, workTime)
            putLong(AppUtils.WAKEUP_TIME, wakeupTime)
            putLong(AppUtils.SLEEPING_TIME_KEY, sleepingTime)
            putBoolean(AppUtils.FIRST_RUN_KEY, false)

            // Calculate total intake and save it in SharedPreferences
            val totalIntake = AppUtils.calculateIntake(weight, workTime)
            val df = DecimalFormat("#").apply { roundingMode = RoundingMode.CEILING }
            putInt(AppUtils.TOTAL_INTAKE, df.format(totalIntake).toInt())

            apply()
        }

        startActivity(Intent(this@InitUserInfoActivity, WaterActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        doubleBackToExitPressedOnce = true

        Snackbar.make(
            this.window.decorView.findViewById(android.R.id.content),
            "Please click BACK again to exit",
            Snackbar.LENGTH_SHORT
        ).show()

        MainScope().launch {
            delay(1000L)
            doubleBackToExitPressedOnce = false
        }
    }
}