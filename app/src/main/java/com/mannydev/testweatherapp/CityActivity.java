package com.mannydev.testweatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mannydev.testweatherapp.adapters.CityAdatper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by manny on 03.10.17.
 */

public class CityActivity extends AppCompatActivity {
    private static final String CITIES = "cities";
    public static ArrayList<String> cityNames;
    private TextView txtResult;
    private Button btnCheck;
    private Button btnSelect;
    private EditText editText;
    private ListView lvCities;
    private String city;
    private SharedPreferences cities;
    private CityAdatper adapter;
    private static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_change_activity);

        cities = getSharedPreferences(CITIES, MODE_PRIVATE);
        txtResult = (TextView) findViewById(R.id.txtResult);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        btnSelect = (Button) findViewById(R.id.btnSelect);
        lvCities = (ListView) findViewById(R.id.lvCities);
        editText = (EditText) findViewById(R.id.editText);
        if (cities.contains(CITIES)) {
            cityNames = loadCities();
        } else cityNames = new ArrayList<>();


        // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
        adapter = new CityAdatper(CityActivity.this, cityNames);

        // Привяжем массив через адаптер к ListView
        lvCities.setAdapter(adapter);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btnSelect.setEnabled(false);
            }
        });
        btnSelect.setEnabled(false);

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                city = editText.getText().toString();
                if (city.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Введите название города!", Toast.LENGTH_LONG).show();
                } else {
                    if (hasConnection(CityActivity.this)) {
                        CityValidChecker cityValidChecker = new CityValidChecker();
                        cityValidChecker.execute();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Проверьте соединение с интернетом!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cityNames.add(editText.getText().toString().toUpperCase());
                adapter = new CityAdatper(CityActivity.this, cityNames);
                lvCities.setAdapter(adapter);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                String save = gson.toJson(cityNames);
                Log.v("JSON", save);
                saveCities();
                Intent intent = new Intent(CityActivity.this, MainActivity.class);
                intent.putExtra("newcity", editText.getText().toString());
                editText.setText("");
                txtResult.setText("");
                startActivity(intent);
            }
        });

    }

    private void saveCities() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String save = gson.toJson(cityNames);
        SharedPreferences.Editor editor = cities.edit();
        editor.clear();
        editor.putString(CITIES, save);
        editor.apply();
    }

    private ArrayList<String> loadCities() {
        ArrayList<String> list = new ArrayList<>();
        JSONArray array;
        if (cities.contains(CITIES)) {
            String jsonCities = cities.getString(CITIES, null);
            try {
                array = new JSONArray(jsonCities);
                for (int i = 0; i < array.length(); i++) {
                    list.add((String) array.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.v("Полученные города:", list.toString());
        return list;
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveCities();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Запоминаем данные
        saveCities();

    }

    @Override
    protected void onResume() {
        super.onResume();
        cityNames = loadCities();

    }

    class CityValidChecker extends AsyncTask<Void, Void, Boolean> {
        String cityWeatherJSON, cityName;


        @Override
        protected void onPreExecute() {
            cityName = city;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            cityWeatherJSON = getWeatherInJSON();
            Log.v("myLogs", "JSONLengh = " + cityWeatherJSON.length() + " " + cityWeatherJSON);

            return cityWeatherJSON.length() != 0;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {
                cityFound();
            } else {
                cityNotFound();
            }
        }


        private String getWeatherInJSON() {
            StringBuilder response = new StringBuilder();
            String sourceUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + editText.getText().toString() + "&APPID=da34a1770ab7804a79a24144a22414d3";
            try {
                URL url = new URL(sourceUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                    String strLine;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }
                httpconn.disconnect();
            } catch (IOException e) {
                txtResult.setText("Город " + editText.getText().toString() + " в базе не найден.");
                editText.setText("");


            }
            return response.toString();
        }


        private void cityFound() {
            txtResult.setText("Город " + editText.getText().toString() + " найден! Добавляйте!;)");
            btnSelect.setEnabled(true);
        }

        private void cityNotFound() {
            txtResult.setText("Город " + editText.getText().toString() + " в базе не найден.");
            editText.setText("");


        }
    }
}
