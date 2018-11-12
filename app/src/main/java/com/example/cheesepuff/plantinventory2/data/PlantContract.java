package com.example.cheesepuff.plantinventory2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Plants app.
 */
public class PlantContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PlantContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.cheesepuff.plantinventory2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PLANTS = "plants";

    // inner class that defines constant values for the plants database table.
    // each entry in the table represents one plant.
    public static abstract class PlantEntry implements BaseColumns {

        private PlantEntry() {

        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLANTS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_PLANTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_PLANTS;

        //Name of table DB
        public final static String TABLE_NAME = "plant";

        //Column of table
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PLANT_NAME = "plant_name";
        public final static String COLUMN_PLANT_PRICE = "price";
        public final static String COLUMN_PLANT_QUANTITY = "quantity";
        public final static String COLUMN_PLANT_SUPPLIER = "supplier";
        public final static String COLUMN_PLANT_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";


    }
}
