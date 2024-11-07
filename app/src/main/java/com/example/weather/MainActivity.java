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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
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
    SQLiteDatabase SQLiteDatabase;
    private List<String> chosenStatesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initializeStateLocationIds();

        SQLiteDatabase = new SQLiteDatabase(this);
//        SQLiteDatabase.clearDatabase();

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

        stateCardViewAdapter = new StateCardViewAdapter(filteredWeatherDataList, SQLiteDatabase);
        recyclerView.setAdapter(stateCardViewAdapter);
        stateCardViewAdapter.notifyDataSetChanged();

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

                Button cancelAddStateButton = dialogView.findViewById(R.id.cancelAddStateButton);
                Button confirmAddStateButton = dialogView.findViewById(R.id.confirmAddStateButton);
                Spinner chooseStateSpinner = dialogView.findViewById(R.id.chooseStateSpinner);

                new HttpGetLocationRequest(chooseStateSpinner).execute("https://api.met.gov.my/v2.1/locations?locationcategoryid=TOWN");
//                String[] states = {"KUALA KRAI", "LOJING", "MACHANG", "PASIR PUTEH", "TANAH MERAH", "TUMPAT", "KOTA BHARU", "JELI", "GUA MUSANG", "BACHOK", "RANTAU PANJANG"
//                        , "PASIR MAS", "SETAPAK", "KUALA LUMPUR", "AMPANG", "BANGSAR", "BUKIT BINTANG", "CHERAS", "JALAN DUTA", "KEPONG"};

//                List<WeatherData> names = SQLiteDatabase.readAllData();
//
//                List<String> townNames = new ArrayList<>();
//
//                for (WeatherData data : names) {
//                    townNames.add(data.getState());
//                }



                cancelAddStateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                confirmAddStateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedState = chooseStateSpinner.getSelectedItem().toString();

                        boolean isStateAlreadyAdded = false;
                        for (WeatherData data : weatherDataList) {
                            if (data.getState().equalsIgnoreCase(selectedState)) {
                                isStateAlreadyAdded = true;
                                break;
                            }
                        }

                        if (isStateAlreadyAdded) {
                            Toast.makeText(MainActivity.this, "State already added!", Toast.LENGTH_SHORT).show();
                        } else {
                            chosenStatesList.add(selectedState);
                            SQLiteDatabase.addTown(selectedState);
                            fetchWeatherDataForState(selectedState);

                            Toast.makeText(MainActivity.this, "State added successfully!", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void initializeStateLocationIds() {
        stateLocationIds = new HashMap<>();
        stateLocationIds.put("MUAR", "LOCATION:1");
        stateLocationIds.put("TANGKAK", "LOCATION:1");
        stateLocationIds.put("LABIS", "LOCATION:1");
        stateLocationIds.put("JOHOR BAHRU", "LOCATION:1");
        stateLocationIds.put("AYER HITAM", "LOCATION:1");
        stateLocationIds.put("BATU PAHAT", "LOCATION:1");
        stateLocationIds.put("MERSING", "LOCATION:1");
        stateLocationIds.put("ISKANDAR PUTERI", "LOCATION:1");
        stateLocationIds.put("KLUANG", "LOCATION:1");
        stateLocationIds.put("NUSAJAYA", "LOCATION:1");
        stateLocationIds.put("KOTA TINGGI", "LOCATION:1");
        stateLocationIds.put("PONTIAN", "LOCATION:1");
        stateLocationIds.put("SEGAMAT", "LOCATION:1");
        stateLocationIds.put("SENAI", "LOCATION:1");
        stateLocationIds.put("SIMPANG RENGGAM", "LOCATION:1");
        stateLocationIds.put("YONG PENG", "LOCATION:1");
        stateLocationIds.put("PASIR GUDANG", "LOCATION:1");
        stateLocationIds.put("KULAI", "LOCATION:1");
        stateLocationIds.put("PAGOH", "LOCATION:1");

        stateLocationIds.put("SERDANG", "LOCATION:2");
        stateLocationIds.put("ALOR STAR", "LOCATION:2");
        stateLocationIds.put("BALING", "LOCATION:2");
        stateLocationIds.put("JITRA", "LOCATION:2");
        stateLocationIds.put("KUALA NERANG", "LOCATION:2");
        stateLocationIds.put("KULIM", "LOCATION:2");
        stateLocationIds.put("PENDANG", "LOCATION:2");
        stateLocationIds.put("POKOK SENA", "LOCATION:2");
        stateLocationIds.put("SIK", "LOCATION:2");
        stateLocationIds.put("SUNGAI PETANI", "LOCATION:2");
        stateLocationIds.put("YAN", "LOCATION:2");

        stateLocationIds.put("KUALA KRAI", "LOCATION:3");
        stateLocationIds.put("MACHANG", "LOCATION:3");
        stateLocationIds.put("LOJING", "LOCATION:3");
        stateLocationIds.put("PASIR PUTEH", "LOCATION:3");
        stateLocationIds.put("TANAH MERAH", "LOCATION:3");
        stateLocationIds.put("TUMPAT", "LOCATION:3");
        stateLocationIds.put("KOTA BHARU", "LOCATION:3");
        stateLocationIds.put("JELI", "LOCATION:3");
        stateLocationIds.put("GUA MUSANG", "LOCATION:3");
        stateLocationIds.put("BACHOK", "LOCATION:3");
        stateLocationIds.put("RANTAU PANJANG", "LOCATION:3");
        stateLocationIds.put("PASIR MAS", "LOCATION:3");

        stateLocationIds.put("SETAPAK", "LOCATION:4");
        stateLocationIds.put("KUALA LUMPUR", "LOCATION:4");
        stateLocationIds.put("AMPANG", "LOCATION:4");
        stateLocationIds.put("BANGSAR", "LOCATION:4");
        stateLocationIds.put("BUKIT BINTANG", "LOCATION:4");
        stateLocationIds.put("CHERAS", "LOCATION:4");
        stateLocationIds.put("JALAN DUTA", "LOCATION:4");
        stateLocationIds.put("KEPONG", "LOCATION:4");
    }

    private void fetchWeatherDataForState(String state) {
        String locationrootid = stateLocationIds.get(state);
        if (locationrootid == null) {
            Toast.makeText(this, "Invalid state selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String weatherUrl = "https://api.met.gov.my/v2.1/locations?locationcategoryid=TOWN&locationrootid=" + locationrootid;
        new HttpGetWeatherRequest(weatherUrl, state).execute();
    }




//    public class HttpGetLocationRequest extends AsyncTask<String, Void, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressBar.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            String stringUrl = params[0];
//            String result = null;
//            try {
//                URL myUrl = new URL(stringUrl);
//                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setReadTimeout(15000);
//                connection.setConnectTimeout(15000);
//                connection.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");
//
//                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                    Log.e("API Error", "HTTP error code: " + connection.getResponseCode());
//                    return null;
//                }
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder stringBuilder = new StringBuilder();
//                String inputLine;
//                while ((inputLine = reader.readLine()) != null) {
//                    stringBuilder.append(inputLine);
//                }
//                reader.close();
//                result = stringBuilder.toString();
//            } catch (IOException e) {
//                Log.e("API Error", "IOException: " + e.getMessage());
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            progressBar.setVisibility(View.GONE);
//
//            if (result == null) {
//                error.setText("Failed to fetch locations.");
//                return;
//            }
//
//            try {
//                JSONObject jsonObject = new JSONObject(result);
//                JSONArray resultsArray = jsonObject.getJSONArray("results");
//
////                weatherDataList.clear();
////                int limit = Math.min(resultsArray.length(), 30);
////                for (int i = 0; i < limit; i++) {
////                    JSONObject resultObject = resultsArray.getJSONObject(i);
////                    String locationName = resultObject.getString("name");
////
////                    WeatherData weatherData = new WeatherData(locationName);
////                    weatherDataList.add(weatherData);
////                }
//
//                weatherDataList.clear();
//                for (int i = 0; i < resultsArray.length(); i++) {
//                    JSONObject resultObject = resultsArray.getJSONObject(i);
//                    String locationName = resultObject.getString("name");
//
////                    SQLiteDatabase.addTown(locationName);
//                    WeatherData weatherData = new WeatherData(locationName);
//                    weatherDataList.add(weatherData);
//
//                }
//
//                filteredWeatherDataList.clear();
//                filteredWeatherDataList.addAll(weatherDataList);
////                stateCardViewAdapter.notifyDataSetChanged();
//
//            } catch (Exception e) {
//                Log.e("HttpGetLocationRequest", "Error parsing locations: " + e.getMessage());
//                error.setText("Error parsing locations. Check log for details.");
//            }
//        }
//    }

    public class HttpGetLocationRequest extends AsyncTask<String, Void, List<String>> {
        private Spinner chooseStateSpinner;

        public HttpGetLocationRequest(Spinner chooseStateSpinner) {
            this.chooseStateSpinner = chooseStateSpinner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected List<String> doInBackground(String... params) {
            List<String> townNames = new ArrayList<>();
            String stringUrl = params[0];
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

                String result = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(result);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    String locationName = resultObject.getString("name");
                    townNames.add(locationName);
                }

            } catch (IOException | JSONException e) {
                Log.e("API Error", "Exception: " + e.getMessage());
            }
            return townNames;
        }

        @Override
        protected void onPostExecute(List<String> townNames) {
            super.onPostExecute(townNames);
            progressBar.setVisibility(View.GONE);

            if (townNames == null || townNames.isEmpty()) {
                error.setText("Failed to fetch locations.");
                return;
            }

            ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, townNames);
            stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chooseStateSpinner.setAdapter(stateAdapter);
        }
    }

        public class HttpGetWeatherRequest extends AsyncTask<String, Void, String> {
        private final String weatherUrl;
        private final String state;

        public HttpGetWeatherRequest(String weatherUrl, String state) {
            this.weatherUrl = weatherUrl;
            this.state = state;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try {
                URL myUrl = new URL(weatherUrl);
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

                List<WeatherData> newWeatherDataList = new ArrayList<>();
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    String locationName = resultObject.getString("name");

                    if (locationName.equalsIgnoreCase(state)) {
                        WeatherData weatherData = new WeatherData(locationName);
                        newWeatherDataList.add(weatherData);
                        break;
                    }
                }

                weatherDataList.addAll(newWeatherDataList);
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

