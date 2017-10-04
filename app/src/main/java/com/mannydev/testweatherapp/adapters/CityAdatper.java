package com.mannydev.testweatherapp.adapters;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mannydev.testweatherapp.CityActivity;
import com.mannydev.testweatherapp.MainActivity;
import com.mannydev.testweatherapp.R;

import java.util.ArrayList;

/**
 * Created by manny on 04.10.17.
 */

public class CityAdatper extends BaseAdapter {
    private final Context ctx;
    private final LayoutInflater lInflater;
    private final ArrayList<String> objects;

    public CityAdatper(Context context, ArrayList<String> itemsList) {
        ctx = context;
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
    public View getView(final int position, View convertView, ViewGroup parent) {


        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_city_name, parent, false);
        }


        final String item = getCity(position);


        // заполняем View в пункте списка данными из Feed:

        ((TextView) view.findViewById(R.id.txtCity)).setText(item);
        view.findViewById(R.id.btnDel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objects.remove(position);
                CityActivity.cityNames.remove(position);
                CityAdatper.super.notifyDataSetChanged();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, MainActivity.class);
                intent.putExtra("newcity", item);
                view.getContext().startActivity(intent);
            }
        });

        return view;
    }

    // Новость по позиции
    private String getCity(int position) {
        return ((String) getItem(position));
    }
}
