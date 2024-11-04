package com.example.weather;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] spinner_items = {"Sort Ascending", "Sort Descending"};
    SearchView searchView;
    RecyclerView recyclerView;
    List<WeatherData> weatherDataList = new ArrayList<>();
    List<WeatherData> filteredWeatherDataList = new ArrayList<>();
    StateCardViewAdapter stateCardViewAdapter;
    ProgressBar progressBar;
    TextView error;
    FloatingActionButton floatingActionButton;
    private Map<String, String> stateLocationIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initializeStateLocationIds();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinner_items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        error = findViewById(R.id.error);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.recyclerView_mainLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stateCardViewAdapter = new StateCardViewAdapter(filteredWeatherDataList);
        recyclerView.setAdapter(stateCardViewAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog_add_states, null);
                dialogBuilder.setView(dialogView);

                AlertDialog alertDialog = dialogBuilder.create();

                Button cancelAddState = dialogView.findViewById(R.id.cancelAddState);
                Button confirmAddState = dialogView.findViewById(R.id.confirmAddState);
                Spinner chooseStateSpinner = dialogView.findViewById(R.id.chooseStateSpinner);
                String[] states = {"Putrajaya", "Perlis", "Sabah", "Sarawak", "Selangor", "Terengganu"};

                ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, states);
                stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                chooseStateSpinner.setAdapter(stateAdapter);

                cancelAddState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                confirmAddState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedState = chooseStateSpinner.getSelectedItem().toString();
                        fetchWeatherDataForState(selectedState);
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        fetchLocations();
    }

    private void initializeStateLocationIds() {
        stateLocationIds = new HashMap<>();
        stateLocationIds.put("Putrajaya", "LOCATION:12");
        stateLocationIds.put("Perlis", "LOCATION:11");
        stateLocationIds.put("Sabah", "LOCATION:13");
        stateLocationIds.put("Sarawak", "LOCATION:14");
        stateLocationIds.put("Selangor", "LOCATION:15");
        stateLocationIds.put("Terengganu", "LOCATION:16");
    }

    private void fetchWeatherDataForState(String state) {
        String locationId = stateLocationIds.get(state);
        if (locationId == null) {
            Toast.makeText(this, "Invalid state selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String weatherUrl = "https://api.met.gov.my/v2.1/locations?locationcategoryid=" + locationId;
        new HttpGetWeatherRequest().execute(weatherUrl);
    }

    private void fetchLocations() {
        String locationsUrl = "https://api.met.gov.my/v2.1/locations?locationcategoryid=STATE";
        new HttpGetLocationRequest().execute(locationsUrl);
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

                weatherDataList.clear();
                int limit = Math.min(resultsArray.length(), 10);
                for (int i = 0; i < limit; i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    String locationName = resultObject.getString("name");

                    WeatherData weatherData = new WeatherData(locationName);
                    weatherDataList.add(weatherData);
                }

                filteredWeatherDataList.clear();
                filteredWeatherDataList.addAll(weatherDataList);
                stateCardViewAdapter.notifyDataSetChanged();

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
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);

            if (result == null) {
                Toast.makeText(MainActivity.this, "Failed to fetch weather data.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    String locationName = resultObject.getString("name");

                    WeatherData weatherData = new WeatherData(locationName);
                    weatherDataList.add(weatherData);
                }

                filteredWeatherDataList.clear();
                filteredWeatherDataList.addAll(weatherDataList);
                stateCardViewAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                Log.e("HttpGetWeatherRequest", "Error parsing weather data: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Error parsing weather data. Check log for details.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void filter(String query) {
        filteredWeatherDataList.clear();
        if (query.isEmpty()) {
            filteredWeatherDataList.addAll(weatherDataList);
        } else {
            for (WeatherData weatherData : weatherDataList) {
                if (weatherData.getState().toLowerCase().contains(query.toLowerCase())) {
                    filteredWeatherDataList.add(weatherData);
                }
            }
        }
        stateCardViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (position == 0) { // Sort by Name Ascending
            Collections.sort(weatherDataList, new Comparator<WeatherData>() {
                @Override
                public int compare(WeatherData o1, WeatherData o2) {
                    return o1.getState().compareToIgnoreCase(o2.getState());
                }
            });
        } else if (position == 1) { // Sort by Name Descending
            Collections.sort(weatherDataList, new Comparator<WeatherData>() {
                @Override
                public int compare(WeatherData o1, WeatherData o2) {
                    return o2.getState().compareToIgnoreCase(o1.getState());
                }
            });
        }

        filteredWeatherDataList.clear();
        filteredWeatherDataList.addAll(weatherDataList);
        stateCardViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}

