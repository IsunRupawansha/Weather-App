package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ListView list_view;
    private String city;
    private String unit;
    private TextView city_id;

    private static final String PREFS_NAME = "WeatherAppPreferences";
    private static final String KEY_CITY = "city";
    private static final String KEY_UNIT = "unit";
    private static final String DEFAULT_CITY = "colombo";
    private static final String DEFAULT_UNIT = "metric"; // Default unit for Celsius
    private static final int SETTINGS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_view = findViewById(R.id.list_view);
        city_id = findViewById(R.id.city_id);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Weather App");

        loadPreferences();
        new WeatherData().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        new WeatherData().execute();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        city = sharedPreferences.getString(KEY_CITY, DEFAULT_CITY);
        String unitPref = sharedPreferences.getString(KEY_UNIT, DEFAULT_UNIT);
        unit = unitPref.equals("metric") ? "metric" : "imperial"; // Ensure correct mapping
        Log.d("MainActivity", "City: " + city + ", Unit: " + unit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.new_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            new WeatherData().execute(); // Refresh the data if settings are updated
        }
    }


    private class WeatherData extends AsyncTask<String, Void, String> {

        HttpURLConnection urlConnection;
        BufferedReader reader;

        String forecastJsonStr;

        @Override
        protected String doInBackground(String... strings) {
            try {
                final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&cnt=20&appid=e1a34a4961de50bc60656424ab1e35ae&units=" + unit;
                URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();



                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("MainActivity", "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (forecastJsonStr == null) {
                Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject weatherObject = new JSONObject(forecastJsonStr);

                String cityName = weatherObject.getJSONObject("city").getString("name");
                city_id.setText(cityName); // Set the city name to the TextView

                JSONArray dataLists = weatherObject.getJSONArray("list");

                int length = dataLists.length();
                String[] data_list = new String[length];
                String[] temp_list = new String[length];
                String[] humidity_list = new String[length];
                Integer[] icon_list = new Integer[length];

                for (int i = 0; i < length; i++) {
                    JSONObject valueObject = dataLists.getJSONObject(i);
                    data_list[i] = valueObject.getString("dt_txt");

                    JSONObject mainObject = valueObject.getJSONObject("main");
                    temp_list[i] = mainObject.getString("temp")+ (unit.equals("metric") ? "°C" : "°F");
                    humidity_list[i] = mainObject.getString("humidity");

                    JSONArray weatherArray = valueObject.getJSONArray("weather");
                    JSONObject weatherArrayObject = weatherArray.getJSONObject(0);
                    icon_list[i] = getApplicationContext().getResources().getIdentifier("pic_" + weatherArrayObject.getString("icon"), "drawable", getApplicationContext().getPackageName());
                }

                CustomListAdapter adapter = new CustomListAdapter(MainActivity.this, data_list, temp_list, icon_list);
                list_view.setAdapter(adapter);

                list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                        Intent dailyActivity = new Intent(MainActivity.this, Weather_Information.class);
                        dailyActivity.putExtra("date", data_list[i]);
                        dailyActivity.putExtra("temperature", temp_list[i]);
                        dailyActivity.putExtra("humidity", humidity_list[i]);
                        dailyActivity.putExtra("icon", icon_list[i]);
                        startActivity(dailyActivity);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
