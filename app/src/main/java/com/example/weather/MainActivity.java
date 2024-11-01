package com.example.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int totalRequests = 0;
    private int completedRequests = 0;

    String[] spinner_items = {"Name", "Temperature"};

    RecyclerView recyclerView;
    List<WeatherData> weatherDataList = new ArrayList<>();
    StateCardViewAdapter stateCardViewAdapter;
    ProgressBar progressBar;
    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinner_items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        error = findViewById(R.id.error);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView_mainLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        stateCardViewAdapter = new StateCardViewAdapter(weatherDataList);
        recyclerView.setAdapter(stateCardViewAdapter);

        fetchLocations();
    }

    private void fetchLocations() {
        String locationsUrl = "https://api.met.gov.my/v2.1/locations?locationcategoryid=STATE";
        new HttpGetLocationRequest().execute(locationsUrl);
    }

    private void fetchWeatherData(String locationId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = dateFormat.format(Calendar.getInstance().getTime());

        String weatherUrl = "https://api.met.gov.my/v2.1/data?datasetid=FORECAST&datacategoryid=GENERAL&locationid="
                + locationId + "&start_date=" + todayDate + "&end_date=" + todayDate;

        new HttpGetWeatherRequest().execute(weatherUrl);
    }

    public class HttpGetLocationRequest extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            String result = null;
            try {
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("API Error", "HTTP error code: " + connection.getResponseCode());
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                reader.close();
                result = stringBuilder.toString();
            } catch (IOException e) {
                Log.e("API Error", "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);

            if (result == null) {
                error.setText("Failed to fetch locations.");
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                totalRequests = resultsArray.length();

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    String locationId = resultObject.getString("id");

                    fetchWeatherData(locationId);
                }

            } catch (Exception e) {
                Log.e("HttpGetLocationRequest", "Error parsing locations: " + e.getMessage());
                error.setText("Error parsing locations. Check log for details.");
            }
        }
    }


    public class HttpGetWeatherRequest extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            String result = null;
            try {
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("API Error", "HTTP error code: " + connection.getResponseCode());
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                reader.close();
                result = stringBuilder.toString();
            } catch (IOException e) {
                Log.e("API Error", "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);

            if (result == null) {
                error.setText("Failed to fetch weather data.");
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray dataArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject weatherObject = dataArray.getJSONObject(i);
                    String locationName = weatherObject.getString("locationname");
                    String date = weatherObject.getString("date");
                    String temperature = weatherObject.getString("value");

                    WeatherData weatherData = new WeatherData(locationName, date, temperature);
                    weatherDataList.add(weatherData);
                    break;
                }

                completedRequests++;

                if (completedRequests == totalRequests) {
                    stateCardViewAdapter.notifyDataSetChanged();
                }

            } catch (Exception e) {
                Log.e("HttpGetWeatherRequest", "Error processing weather data: " + e.getMessage());
                error.setText("Error fetching weather data. Check log for details.");
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        // sorting based on name, temperature
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
