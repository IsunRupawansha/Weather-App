package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private EditText location;
    private Spinner spinner;
    private SharedPreferences sharedPreferences;
    private String selectedUnit;

    private static final String PREFS_NAME = "WeatherAppPreferences";
    private static final String KEY_CITY = "city";
    private static final String KEY_UNIT = "unit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        location = findViewById(R.id.location);
        spinner = findViewById(R.id.spinner);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Units, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Load existing settings
        String city = sharedPreferences.getString(KEY_CITY, "colombo");
        String unit = sharedPreferences.getString(KEY_UNIT, "metric");

        location.setText(city);

        // Set spinner selection based on saved unit
        if (unit.equals("metric")) {
            spinner.setSelection(0); // Celsius
        } else if (unit.equals("imperial")) {
            spinner.setSelection(1); // Fahrenheit
        }

        // Handle spinner item selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedUnit = (position == 0) ? "Celsius" : "Fahrenheit";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Get user input and update SharedPreferences
        String city = location.getText().toString().trim();
        selectedUnit = (spinner.getSelectedItemPosition() == 0) ? "Celsius" : "Fahrenheit";

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_UNIT, selectedUnit.equals("Celsius") ? "metric" : "imperial");
        editor.apply();

        // Send result back to MainActivity
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(Settings.this, "Settings saved", Toast.LENGTH_SHORT).show();
        super.onBackPressed(); // Call super to handle back press
    }
}
