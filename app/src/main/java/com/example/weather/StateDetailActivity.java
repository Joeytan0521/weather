package com.example.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
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

public class StateDetailActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView error;
    private RecyclerView nestedRecyclerView;
    private TextView stateName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_detail_layout);

        stateName = findViewById(R.id.stateName);
        progressBar = findViewById(R.id.progressBar);
        error = findViewById(R.id.errorTextView);
        nestedRecyclerView = findViewById(R.id.recyclerview_parent);

        String receivedStateName = getIntent().getStringExtra("STATE_NAME");
        if (receivedStateName != null) {
            stateName.setText(receivedStateName + " 7-Day Weather Forecast");
            fetchWeatherData(receivedStateName);
        } else {
            error.setText("State name is not provided.");
        }
    }

    private void fetchWeatherData(String stateName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = dateFormat.format(Calendar.getInstance().getTime());

        String weatherUrl = "https://api.met.gov.my/v2.1/data?datasetid=FORECAST&datacategoryid=GENERAL&locationname="
                + stateName + "&start_date=" + todayDate + "&end_date=" + todayDate;

        new HttpGetWeatherRequest().execute(weatherUrl);
    }

    // AsyncTask to fetch weather data
    private class HttpGetWeatherRequest extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            StringBuilder result = new StringBuilder();
            try {
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("API Error", "HTTP error code: " + responseCode);
                    Log.e("API Error", "Response Message: " + connection.getResponseMessage());
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    result.append(inputLine);
                }
                reader.close();
            } catch (IOException e) {
                Log.e("API Error", "IOException: " + e.getMessage());
            }
            return result.toString();
        }


        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);

            if (result == null) {
                error.setText("Failed to fetch weather data.");
                error.setVisibility(View.VISIBLE);
                return;
            }
            Log.d("API Response", result);

            List<ChildModelClass> childData = parseWeatherData(result);
            setupRecyclerView(childData);
        }

        private List<ChildModelClass> parseWeatherData(String jsonData) {
            List<ChildModelClass> childData = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray dataArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject weatherObject = dataArray.getJSONObject(i);
                    String temperature = weatherObject.getString("value");
                    childData.add(new ChildModelClass(temperature));
                }
            } catch (Exception e) {
                Log.e("JsonParsingError", "Error parsing JSON data: " + e.getMessage());
                error.setText("Error processing weather data. Check log for details.");
            }
            return childData;
        }
    }

    private void setupRecyclerView(List<ChildModelClass> childData) {
        List<ParentModelClass> parentData = new ArrayList<>();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        parentData.add(new ParentModelClass(date, childData));

        nestedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ParentAdapter adapter = new ParentAdapter(parentData, this);
        nestedRecyclerView.setAdapter(adapter);
    }

}