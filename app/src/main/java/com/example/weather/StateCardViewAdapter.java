package com.example.weather;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StateCardViewAdapter extends RecyclerView.Adapter<StateCardViewAdapter.ViewHolder> {
    private List<WeatherData> weatherDataList;
    private SQLiteDatabase SQLiteDatabase;
    Context context;

    public StateCardViewAdapter(List<WeatherData> weatherDataList, SQLiteDatabase SQLiteDatabase) {
        this.weatherDataList = weatherDataList;
        this.SQLiteDatabase = SQLiteDatabase;

    }

    @NonNull
    @Override
    public StateCardViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_layout_state_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateCardViewAdapter.ViewHolder holder, int position) {
        WeatherData weatherData = weatherDataList.get(position);
        holder.state.setText(weatherData.getState());

        holder.state.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), StateDetailActivity.class);
            intent.putExtra("STATE_NAME", weatherData.getState());
            v.getContext().startActivity(intent);
        });

        holder.deleteTown_button.setOnClickListener(v -> {
            String townName = weatherData.getState();

            SQLiteDatabase.deleteData(townName);

            weatherDataList.remove(position);
            notifyItemRemoved(position);
        });

//        holder.deleteTown_button.setOnClickListener(v -> {
//            int townId = weatherData.getId();
//
//            SQLiteDatabase dbHelper = new SQLiteDatabase(context);
//            dbHelper.deleteData(townId);
//
//            weatherDataList.remove(position);
//            notifyItemRemoved(position);
//        });
    }

    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView state;
        public Button deleteTown_button;

        public ViewHolder(View itemView) {
            super(itemView);
            state = itemView.findViewById(R.id.state);
            deleteTown_button = itemView.findViewById(R.id.deleteTown_button);
        }
    }

}
