package com.cmpe277.bitensip

import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import com.cmpe277.bitensip.databinding.ActivityInitUserInfoBinding

class InitUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitUserInfoBinding  // Correct binding class for this layout
    private var weight: String = ""
    private var workTime: String = ""
    private var wakeupTime: Long = 0
    private var sleepingTime: Long = 0
    private lateinit var sharedPref: SharedPreferences
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding properly here
        binding = ActivityInitUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Set the content view using binding.root

        val is24h = DateFormat.is24HourFormat(this.applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        sharedPref = getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)

        wakeupTime = sharedPref.getLong(AppUtils.WAKEUP_TIME, 1558323000000)
        sleepingTime = sharedPref.getLong(AppUtils.SLEEPING_TIME_KEY, 1558369800000)

        // Set up TimePicker for WakeUpTime EditText
        binding.etWakeUpTime.editText!!.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = wakeupTime

            val mTimePicker: TimePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val time = Calendar.getInstance()
                    time.set(Calendar.HOUR_OF_DAY, selectedHour)
                    time.set(Calendar.MINUTE, selectedMinute)
                    wakeupTime = time.timeInMillis

                    binding.etWakeUpTime.editText!!.setText(
                        String.format("%02d:%02d", selectedHour, selectedMinute)
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24h
            )
            mTimePicker.setTitle("Select Wakeup Time")
            mTimePicker.show()
        }

        // Set up TimePicker for SleepTime EditText
        binding.etSleepTime.editText!!.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = sleepingTime

            val mTimePicker: TimePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val time = Calendar.getInstance()
                    time.set(Calendar.HOUR_OF_DAY, selectedHour)
                    time.set(Calendar.MINUTE, selectedMinute)
                    sleepingTime = time.timeInMillis

                    binding.etSleepTime.editText!!.setText(
                        String.format("%02d:%02d", selectedHour, selectedMinute)
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24h
            )
            mTimePicker.setTitle("Select Sleeping Time")
            mTimePicker.show()
        }

        // Handle Continue button click event
        binding.btnContinue.setOnClickListener { view ->
            // Hide the keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.initUserInfoParentLayout.windowToken, 0)

            // Retrieve input values safely
            val weight = binding.etWeight.editText?.text.toString().trim()
            val workTime = binding.etWorkTime.editText?.text.toString().trim()

            // Helper function to show Snackbar messages easily
            fun showSnackbar(message: String) {
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
            }

            // Validate input fields before proceeding
            when {
                weight.isNullOrBlank() -> showSnackbar("Please input your weight")
                weight.toIntOrNull() == null || weight.toInt() !in 20..200 -> showSnackbar("Please input a valid weight between 20 and 200")
                workTime.isNullOrBlank() -> showSnackbar("Please input your workout time")
                workTime.toIntOrNull() == null || workTime.toInt() !in 0..500 -> showSnackbar("Please input a valid workout time between 0 and 500")
                else -> {
                    // Save data using SharedPreferences after validation passes
                    sharedPref.edit().apply {
                        putInt(AppUtils.WEIGHT_KEY, weight.toInt())
                        putInt(AppUtils.WORK_TIME_KEY, workTime.toInt())
                        putLong(AppUtils.WAKEUP_TIME, wakeupTime)
                        putLong(AppUtils.SLEEPING_TIME_KEY, sleepingTime)
                        putBoolean(AppUtils.FIRST_RUN_KEY, false)

                        // Calculate total intake and save it in SharedPreferences
                        val totalIntake = AppUtils.calculateIntake(weight.toInt(), workTime.toInt())
                        val df = DecimalFormat("#").apply { roundingMode = RoundingMode.CEILING }
                        putInt(AppUtils.TOTAL_INTAKE, df.format(totalIntake).toInt())

                        apply()
                    }

                    // Navigate to MainActivity after saving data and finish current activity.
                    startActivity(Intent(this, WaterActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Snackbar.make(
            this.window.decorView.findViewById(android.R.id.content),
            "Please click BACK again to exit",
            Snackbar.LENGTH_SHORT
        ).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }
}