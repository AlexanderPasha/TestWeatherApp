package com.mannydev.testweatherapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mannydev.testweatherapp.CityWeather;
import com.mannydev.testweatherapp.R;

import java.util.ArrayList;

/**
 * Адаптер для отображения новостей
 */

public class WeatherAdapter extends BaseAdapter {
    private final LayoutInflater lInflater;
    private final ArrayList<CityWeather> objects;

    public WeatherAdapter(Context context, ArrayList<CityWeather> itemsList) {
        Context ctx = context;
        objects = itemsList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_city_weather, parent, false);
        }

        final CityWeather item = getRss(position);

        // заполняем View в пункте списка данными из Feed:

        ((TextView) view.findViewById(R.id.txtCityName)).setText(item.getName());
        ((TextView) view.findViewById(R.id.txtTemperature)).setText(item.getTemperature());
        ((TextView) view.findViewById(R.id.txtWindValue)).setText(item.getWind());
        ((TextView) view.findViewById(R.id.txtCloudinessValue)).setText(item.getCloudiness());
        ((TextView) view.findViewById(R.id.txtHumidityValue)).setText(item.getHumidity());
        ((TextView) view.findViewById(R.id.txtPressureValue)).setText(item.getPressure());
        ((TextView) view.findViewById(R.id.txtMain)).setText(item.getMain());
        return view;
    }

    // Новость по позиции
    private CityWeather getRss(int position) {
        return ((CityWeather) getItem(position));
    }
}
