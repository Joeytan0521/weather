package com.example.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StateCardViewAdapter extends RecyclerView.Adapter<StateCardViewAdapter.ViewHolder> {
    private List<WeatherData> weatherDataList;

    public StateCardViewAdapter(List<WeatherData> weatherDataList) {
        this.weatherDataList = weatherDataList;
    }

    @NonNull
    @Override
    public StateCardViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_layout_state_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateCardViewAdapter.ViewHolder holder, int position) {
        holder.state.setText(weatherDataList.get(position).getState());
        holder.date.setText(weatherDataList.get(position).getDate());
        holder.temp.setText(weatherDataList.get(position).getTemp());
    }

    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView state;
        public TextView date;
        public TextView temp;


        public ViewHolder(View itemView) {
            super(itemView);
            state = itemView.findViewById(R.id.state);
            date = itemView.findViewById(R.id.date);
            temp = itemView.findViewById(R.id.temperature);
        }
    }

}
