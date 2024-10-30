package com.example.weather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<ParentModelClass> parentModelClassArrayList;
    ArrayList<ChildModelClass> favouriteList;
    ArrayList<ChildModelClass> recentlyWatchedList;
    ArrayList<ChildModelClass> latestList;
    ParentAdapter parentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_weather_info_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerview_parent);
        favouriteList = new ArrayList<>();
        recentlyWatchedList = new ArrayList<>();
        latestList = new ArrayList<>();
        parentModelClassArrayList = new ArrayList<>();


//        latestList.add(new ChildModelClass(R.drawable.poster_2));
//        latestList.add(new ChildModelClass(R.drawable.poster_3));
//        latestList.add(new ChildModelClass(R.drawable.poster_4));
//        latestList.add(new ChildModelClass(R.drawable.poster_5));
        parentModelClassArrayList.add(new ParentModelClass("..", latestList));


//        recentlyWatchedList.add(new ChildModelClass(R.drawable.poster_6));
//        recentlyWatchedList.add(new ChildModelClass(R.drawable.poster_7));
//        recentlyWatchedList.add(new ChildModelClass(R.drawable.poster_8));
//        recentlyWatchedList.add(new ChildModelClass(R.drawable.poster_9));
        parentModelClassArrayList.add(new ParentModelClass("..", recentlyWatchedList));


//        favouriteList.add(new ChildModelClass(R.drawable.poster_10));
//        favouriteList.add(new ChildModelClass(R.drawable.poster_11));
//        favouriteList.add(new ChildModelClass(R.drawable.poster_12));
//        favouriteList.add(new ChildModelClass(R.drawable.poster_13));
        parentModelClassArrayList.add(new ParentModelClass("..", favouriteList));


        parentAdapter = new ParentAdapter(parentModelClassArrayList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parentAdapter);
    }
}
