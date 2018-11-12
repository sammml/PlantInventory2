package com.example.cheesepuff.plantinventory2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cheesepuff.plantinventory2.data.PlantContract.PlantEntry;

/**
 * Database helper for Plants app. Manages database creation and version management.
 */
public class PlantDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PlantDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "plant.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public PlantDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a string that contains the SQL statement to create the plants table
        String SQL_CREATE_PLANT_TABLE = "CREATE TABLE " + PlantEntry.TABLE_NAME + " ("
                + PlantEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PlantEntry.COLUMN_PLANT_NAME + " TEXT NOT NULL, "
                + PlantEntry.COLUMN_PLANT_PRICE + " INTEGER NOT NULL, "
                + PlantEntry.COLUMN_PLANT_QUANTITY + " INTEGER NOT NULL, "
                + PlantEntry.COLUMN_PLANT_SUPPLIER + " INTEGER NOT NULL DEFAULT 0, "
                + PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER + " INTEGER );";

        //execute the SQL statement
        db.execSQL(SQL_CREATE_PLANT_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
        // if (oldVersion <2) {}
    }
}

