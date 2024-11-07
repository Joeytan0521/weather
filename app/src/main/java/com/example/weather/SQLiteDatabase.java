package com.example.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase extends SQLiteOpenHelper {

    List<WeatherData> weatherDataList = new ArrayList<>();
    private Context context;
    private static final String DATABASE_NAME = "TownData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "towns";
    private static final String TOWN_ID = "_id";
    private static final String TOWN_NAME = "town_name";

    public SQLiteDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME + " ("
                + TOWN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TOWN_NAME + " TEXT);";

//        String query =
//                "CREATE TABLE " + TABLE_NAME + " ("
//                        + TOWN_NAME + " TEXT PRIMARY KEY); ";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<WeatherData> readAllData(){
        weatherDataList.clear();
        String query = "SELECT * FROM " + TABLE_NAME;
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()){
            do{
                String locationName = cursor.getString(cursor.getColumnIndex(TOWN_NAME));

                WeatherData weatherData = new WeatherData(locationName);
                weatherDataList.add(weatherData);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return weatherDataList;
    }

//    void deleteData(int id) {
//        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
//        int result = db.delete(TABLE_NAME, TOWN_ID + " = ?", new String[]{String.valueOf(id)});
//
//        if (result == -1) {
//            Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
//        }
//        db.close();
//    }
    void deleteData(String name){
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, "town_name = ?", new String[]{name});

        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Successfully deleted data", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }



    public void addTown(String townName) {
        if (!isTownExists(townName)) {
            android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(TOWN_NAME, townName);
            db.insert(TABLE_NAME, null, cv);
            db.close();
        } else {
            Toast.makeText(context, "Town already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isTownExists(String townName) {
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, TOWN_NAME + "=?", new String[]{townName}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public void clearDatabase() {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

}
