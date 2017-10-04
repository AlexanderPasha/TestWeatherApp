package com.mannydev.testweatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mannydev.testweatherapp.adapters.WeatherAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private static final String APP_MEM = "weather";
    private static final String CITY_NAME = "cityName";
    private static final String CITY = "location";
    private static final String TEMPERATURE = "temp";
    private static final String WIND = "wind";
    private static final String PRESSURE = "pressure";
    private static final String HUMIDITY = "humidity";
    private static final String CLOUDINESS = "cloudiness";
    private static final String MAIN = "main";

    private ArrayList<CityWeather> cwList;
    private SharedPreferences weather;
    private ListView lvMain;
    private String city;
    private Button btnRefresh;
    private Button btnChangeCity;
    private CityWeather location;

    //Проверка устройства на наличия связи с интернетом
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
        setContentView(R.layout.main);
        weather = getSharedPreferences(APP_MEM, MODE_PRIVATE);
        lvMain = (ListView) findViewById(R.id.lvMain);
        cwList = new ArrayList<>();
        Intent intent = getIntent();
        String newcity = intent.getStringExtra("newcity");

        //Проверяем , первый ли это запуск приложения
        if (newcity != null || weather.contains(CITY_NAME)) {
            city = weather.getString(CITY_NAME, null);
            //Проверяем, не изменили ли мы только что наш город
            if (newcity != null && !Objects.equals(newcity, city)) {
                if (hasConnection(MainActivity.this)) {
                    city = newcity;
                    CityWeatherGeter cwg = new CityWeatherGeter();
                    cwg.execute();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                getCacheOnTop();
            }
        } else {
            Log.v("myLogs", "Первый запуск приложения =true");
            Intent newintent = new Intent(MainActivity.this, CityActivity.class);
            startActivity(newintent);
        }
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnChangeCity = (Button) findViewById(R.id.btnChangeCity);

        btnChangeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CityActivity.class);
                startActivity(intent);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasConnection(MainActivity.this)) {
                    CityWeatherGeter cwg = new CityWeatherGeter();
                    cwg.execute();


                } else {
                    Toast.makeText(getApplicationContext(),
                            "Нет соединения с интернетом!", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    //Подгружаем последние данные (кеш)
    private void getCacheOnTop() {

        Log.v("myLogs", "Подгружаем кеш");
        cwList = new ArrayList<>();
        cwList.add(getLastWeather());
        WeatherAdapter adptr = new WeatherAdapter(this, cwList);
        lvMain.setAdapter(adptr);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Запоминаем данные
        rememberData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        location = getLastWeather();

    }

    //Сохраняем данные с экрана
    private void rememberData() {
        SharedPreferences.Editor editor = weather.edit();
        editor.putString(CITY_NAME, city);
        editor.putString(MAIN, location.getMain());
        editor.putString(CITY, location.getName());
        editor.putString(TEMPERATURE, location.getTemperature());
        editor.putString(WIND, location.getWind());
        editor.putString(PRESSURE, location.getPressure());
        editor.putString(CLOUDINESS, location.getCloudiness());
        editor.putString(HUMIDITY, location.getHumidity());
        editor.apply();
    }

    //Получаем данные из кеш
    private CityWeather getLastWeather() {
        CityWeather lastWeather = new CityWeather();
        if (weather.contains(CITY)) {
            lastWeather.setName(weather.getString(CITY, null));
        }
        if (weather.contains(MAIN)) {
            lastWeather.setMain(weather.getString(MAIN, null));
        }
        if (weather.contains(TEMPERATURE)) {
            lastWeather.setTemperature(weather.getString(TEMPERATURE, null));
        }
        if (weather.contains(WIND)) {
            lastWeather.setWind(weather.getString(WIND, null));
        }
        if (weather.contains(PRESSURE)) {
            lastWeather.setPressure(weather.getString(PRESSURE, null));
        }
        if (weather.contains(CLOUDINESS)) {
            lastWeather.setCloudiness(weather.getString(CLOUDINESS, null));
        }
        if (weather.contains(HUMIDITY)) {
            lastWeather.setHumidity(weather.getString(HUMIDITY, null));
        }
        return lastWeather;
    }

    //Класс для получения погодных данных
    class CityWeatherGeter extends AsyncTask<Void, Void, CityWeather> {
        String cityWeatherJSON, cityName;
        private JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            cityName = city;
        }

        @Override
        protected CityWeather doInBackground(Void... voids) {

            cityWeatherJSON = getWeatherInJSON(cityName);
            CityWeather cityWeather = null;
            try {
                cityWeather = new CityWeather();
                jsonObject = new JSONObject(cityWeatherJSON);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject wind = jsonObject.getJSONObject("wind");
                JSONObject sys = jsonObject.getJSONObject("sys");
                JSONArray weather = jsonObject.getJSONArray("weather");
                JSONObject description = weather.getJSONObject(0);

                cityWeather.setName(jsonObject.getString("name") + ", " + sys.getString("country"));
                cityWeather.setWind(wind.getString("speed") + "m/s");
                cityWeather.setTemperature(getCelcius(main.getString("temp")) + " °C");
                cityWeather.setCloudiness(description.getString("description"));
                cityWeather.setMain("(" + description.getString("main") + ")");
                cityWeather.setHumidity(main.getString("humidity") + " %");
                cityWeather.setPressure(main.getString("pressure") + " hPa");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return cityWeather;

        }

        @Override
        protected void onPostExecute(CityWeather result) {
            if (result != null) {
                cwList = new ArrayList<>();
                cwList.add(result);
                WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, cwList);
                lvMain.setAdapter(adapter);
                location = result;

                //перезаписываем кеш
                rememberData();
            } else {
                getCacheOnTop();
                Toast.makeText(MainActivity.this, "Проверьте соединение с интернетом", Toast.LENGTH_SHORT).show();
            }

        }

        //Получаем данные по городу с сайта
        private String getWeatherInJSON(String city) {
            StringBuilder response = new StringBuilder();
            String sourceUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&APPID=da34a1770ab7804a79a24144a22414d3";
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
                Log.d("myLog", "" + e.toString());

            }
            return response.toString();
        }

        //Переводим в градусы Цельсия
        private String getCelcius(String temp) {
            double d = Double.parseDouble(temp);
            double c = d - 273.15;
            return String.valueOf(Math.round(c));
        }

    }

}
