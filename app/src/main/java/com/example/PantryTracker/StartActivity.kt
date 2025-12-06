package com.example.PantryTracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
class StartActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        prefs = getSharedPreferences("PantryPrefs", MODE_PRIVATE)



        val themeGroup = findViewById<RadioGroup>(R.id.themeGroup)


        val themePink = findViewById<RadioButton>(R.id.themePink)
        val themeBlue = findViewById<RadioButton>(R.id.themeBlue)

        val saveButton = findViewById<Button>(R.id.saveButton)


        saveButton.setOnClickListener {
            val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
            val username = usernameEditText.text.toString().trim()
            val editor = prefs.edit()
            val selectedThemeId = themeGroup.checkedRadioButtonId

            editor.putString("username", username)

            if (selectedThemeId != -1) {
                editor.putString("theme", if (selectedThemeId == R.id.themePink) "pink" else "blue")
            }



            editor.apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
