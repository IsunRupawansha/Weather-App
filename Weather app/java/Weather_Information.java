package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Weather_Information extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather_information);

        Intent intent = getIntent();

        TextView txtdate =(TextView) findViewById(R.id.date_list1);
        TextView txttemp =(TextView) findViewById(R.id.temp_list1);
        TextView txthumidity =(TextView) findViewById(R.id.humidity_list1);
        ImageView imgicon = (ImageView) findViewById(R.id.icon_list1);

        txtdate.setText(intent.getStringExtra("date"));
        txttemp.setText(intent.getStringExtra("temperature")+" ");
        txthumidity.setText("Humidity: " + intent.getStringExtra("humidity")+ "%");
        imgicon.setImageResource(intent.getIntExtra("icon",R.drawable.pic_01d));

    }
}