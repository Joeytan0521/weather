package com.example.weather;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherData {
    private String state;
    private String date;
    private String temp;

    public WeatherData(String state, String date, String temp) {
        this.state = state;
        this.date = formatDateTime(date);
        this.temp = temp;
    }

    public String getState() {
        return state;
    }

    public String getDate() {
        return date;
    }

    public String getTemp() {
        return temp;
    }

    private String formatDateTime(String rawDate) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Date date = originalFormat.parse(rawDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

            return dateFormatter.format(date) + " " + timeFormatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return rawDate;
        }
    }

}
