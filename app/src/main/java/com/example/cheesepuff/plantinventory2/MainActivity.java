package com.example.cheesepuff.plantinventory2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cheesepuff.plantinventory2.data.PlantContract.PlantEntry;


/**
 * Displays list of plants that were entered and stored in the app.
 */
public class MainActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PLANT_LOADER = 0;
    private PlantCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the ListView which will be populated with the plant data
        ListView plantListView = (ListView) findViewById(R.id.list);

        // find n set empty view on the ListVIew, so thats only shows when the list has 0 item.
        View emptyView = findViewById(R.id.empty_view);
        plantListView.setEmptyView(emptyView);

        FloatingActionButton plus = findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // setup an Adapter to create a list item for each row of plant data in the Cursor.
        // there is no plant data yet until the loader finishes so pass in null for the Cursor.
        mCursorAdapter = new PlantCursorAdapter(this, null);
        plantListView.setAdapter(mCursorAdapter);

        // setup the item click listener
        plantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d("dp", "Clicked on an item in the Plant list");
                Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentPlantUri = ContentUris.withAppendedId(PlantEntry.CONTENT_URI, id);
                editorIntent.setData(currentPlantUri);
                startActivity(editorIntent);
            }
        });

        // kick off the loader
        getLoaderManager().initLoader(PLANT_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PlantEntry._ID,
                PlantEntry.COLUMN_PLANT_NAME,
                PlantEntry.COLUMN_PLANT_PRICE,
                PlantEntry.COLUMN_PLANT_QUANTITY,
                PlantEntry.COLUMN_PLANT_SUPPLIER,
                PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                PlantEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // update with this new cursor containing update plant data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyPlant();
                return true;

            case R.id.action_delete_all_entries:
                deleteAllPlants();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // helper method to insert hardcoded plant data into the database. for debugging purposes only.
    private void insertDummyPlant() {
            // dummy plant1
            ContentValues values = new ContentValues();
            values.put(PlantEntry.COLUMN_PLANT_NAME, getString(R.string.plant1_name));
            values.put(PlantEntry.COLUMN_PLANT_PRICE, Double.parseDouble(getString(R.string.plant1_price)));
            values.put(PlantEntry.COLUMN_PLANT_QUANTITY, Integer.parseInt(getString(R.string.plant1_quantity)));
            values.put(PlantEntry.COLUMN_PLANT_SUPPLIER, getString(R.string.plant1_supplier));

            values.put(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER, getString(R.string.plant1_supplierPhone));

            getContentResolver().insert(PlantEntry.CONTENT_URI, values);


    }

    // look for current plant counts
    private int getCurrentDbRowCount() {
        // look into query database

        // Just get the COUNT (like SELECT COUNT(*) FROM plant).
        Cursor countCursor = getContentResolver().query(
                PlantEntry.CONTENT_URI,
                new String[] {"count(*) AS count"},
                null,
                null,
                null
        );

        // get total plant count
        countCursor.moveToFirst();
        return countCursor.getInt(0);
    }

    private void deleteAllPlants() {
        int rowsDeleted = getContentResolver().delete(PlantEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, getString(R.string.delete_total) + String.valueOf(rowsDeleted), Toast.LENGTH_SHORT).show();

        }
    }

}