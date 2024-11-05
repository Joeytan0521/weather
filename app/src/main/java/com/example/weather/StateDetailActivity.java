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
    private List<ParentModelClass> parentData = new ArrayList<>();
    private static final int TOTAL_DAYS = 7;

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
            stateName.setText(receivedStateName + " 7-Days Weather Forecast");
            fetchDailyWeatherData(receivedStateName);
        } else {
            error.setText("State name is not provided.");
        }
    }

    private void fetchDailyWeatherData(String stateName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < TOTAL_DAYS; i++) {
            String date = dateFormat.format(calendar.getTime());
            new HttpGetWeatherRequest(stateName, date).execute();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private class HttpGetWeatherRequest extends AsyncTask<Void, Void, String> {
        private final String stateName;
        private final String date;

        public HttpGetWeatherRequest(String stateName, String date) {
            this.stateName = stateName;
            this.date = date;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            String weatherUrl = "https://api.met.gov.my/v2.1/data?datasetid=FORECAST&datacategoryid=GENERAL&locationname="
                    + stateName + "&start_date=" + date + "&end_date=" + date;
            StringBuilder result = new StringBuilder();
            try {
                URL myUrl = new URL(weatherUrl);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("API Error", "HTTP error code: " + responseCode);
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
                error.setText("Failed to fetch weather data for " + date);
                error.setVisibility(View.VISIBLE);
                return;
            }

            List<ChildModelClass> childData = parseWeatherData(result);
            addWeatherDataToRecyclerView(date, childData);
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
            }
            return childData;
        }
    }

    private void addWeatherDataToRecyclerView(String date, List<ChildModelClass> childData) {
        parentData.add(new ParentModelClass(date, childData));

        if (nestedRecyclerView.getAdapter() == null) {
            ParentAdapter adapter = new ParentAdapter(parentData, this);
            nestedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            nestedRecyclerView.setAdapter(adapter);
        } else {
            nestedRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
