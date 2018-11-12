package com.example.cheesepuff.plantinventory2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.cheesepuff.plantinventory2.R;
import com.example.cheesepuff.plantinventory2.data.PlantContract.PlantEntry;

public class PlantProvider extends ContentProvider {

    public static final String LOG_TAG = PlantProvider.class.getSimpleName();
    private static final int PLANTS = 100;
    private static final int PLANT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLANTS, PLANTS);
        sUriMatcher.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLANTS + "/#", PLANT_ID);
    }

    private PlantDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PlantDbHelper((getContext()));
        return true;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {

            case PLANTS:
                cursor = database.query(PlantEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case PLANT_ID:
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PlantEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_query_uri) + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return insertPlant(uri, contentValues);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.insertion_not_supported) + uri);
        }
    }

    // insert plant into the database with the given content values.
    // return the new content URI for the specific row in the database
    private Uri insertPlant(Uri uri, ContentValues values) {
        String name = values.getAsString(PlantEntry.COLUMN_PLANT_NAME);
        Double price = values.getAsDouble(PlantEntry.COLUMN_PLANT_PRICE);
        Integer quantity = values.getAsInteger(PlantEntry.COLUMN_PLANT_QUANTITY);
        String supplier = values.getAsString(PlantEntry.COLUMN_PLANT_SUPPLIER);
        Integer supplierPhone = values.getAsInteger(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER);

        if (name == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.requires_name));
        }
        if (price != null && price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.requires_price));
        }

        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.requires_quantity));
        }

        if (supplier == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.requires_supplier));
        }

        if (supplierPhone != null && supplierPhone < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.requires_supplier_phone));
        }

        // insert a new plant
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(PlantEntry.TABLE_NAME, null, values);
        if (id == -1) {
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return updatePlant(uri, contentValues, selection, selectionArgs);
            case PLANT_ID:
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePlant(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);

        }
    }

    private int updatePlant(Uri uri,
                            ContentValues values,
                            String selection,
                            String[] selectionArgs) {

        if (values.containsKey(PlantEntry.COLUMN_PLANT_NAME)) {
            String name = values.getAsString(PlantEntry.COLUMN_PLANT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.requires_name));
            }
        }
        if (values.containsKey(PlantEntry.COLUMN_PLANT_PRICE)) {
            Integer price = values.getAsInteger(PlantEntry.COLUMN_PLANT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.requires_price));
            }
        }

        if (values.containsKey(PlantEntry.COLUMN_PLANT_QUANTITY)) {
            Integer quantity = values.getAsInteger(PlantEntry.COLUMN_PLANT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.requires_quantity));
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PlantEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri,
                      String selection,
                      String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PLANTS:
                rowsDeleted = database.delete(PlantEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PLANT_ID:
                selection = PlantEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PlantEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANTS:
                return PlantEntry.CONTENT_LIST_TYPE;
            case PLANT_ID:
                return PlantEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + "with match " + match);
        }
    }
}
