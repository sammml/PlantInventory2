package com.example.cheesepuff.plantinventory2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cheesepuff.plantinventory2.data.PlantContract.PlantEntry;

public class PlantCursorAdapter extends CursorAdapter {

    private String plantName;
    private Double plantPrice;
    private int plantQuantity;
    private String plantSupplier;
    private String plantSupplierPhone;

    static class ViewHolder {
        TextView plantName;
        TextView plantPrice;
        TextView plantQuantity;
    }

    public PlantCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /** this method binds the plant data (in the current row pointed to by cursor) to the given
     * list item layout. for ex. the na me for the current plant can be set on the name TextView
     * in the list item layout
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView (final View view, final Context context, final Cursor cursor) {

        ViewHolder holder = new ViewHolder();

        holder.plantName = view.findViewById(R.id.plant_name);
        holder.plantPrice = view.findViewById(R.id.plant_price);
        holder.plantQuantity = view.findViewById(R.id.plant_quantity);

        final ImageView sellButton = view.findViewById(R.id.button_sale);
        final TextView btnPlantName = view.findViewById(R.id.plant_name);

        // find the columns of pet attributes that we are interested in
        int nameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER);

        plantName = cursor.getString(nameColumnIndex);
        plantPrice = cursor.getDouble(priceColumnIndex);
        plantQuantity = cursor.getInt(quantityColumnIndex);

        plantSupplier = cursor.getString(supplierColumnIndex);
        plantSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

        // Plant name button.
        btnPlantName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dp", "hey I clicked on the plant name");

                // Sammie: Do something here to launch the Editing intent for this item.
            };
        });

        // Price tag button.
        sellButton.setTag(R.id.plant_id, cursor.getString(cursor.getColumnIndex(PlantEntry._ID)));
        sellButton.setTag(R.id.plant_name, cursor.getString(cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME)));
        sellButton.setTag(R.id.plant_quantity, cursor.getString(cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_QUANTITY)));

        // no supplier provided to unknown
        if (TextUtils.isEmpty(plantSupplier)) {
            plantSupplier = context.getString(R.string.supplier_unknown);
        }
        // avoid quantity to go below 0
        if (plantQuantity <=0) {
            plantQuantity = 0;
        }

        //set up for the sell button
        sellButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("dp", "Clicked on the price tag button");

                // get data of plant
                String currentPlantIdTag = (String) sellButton.getTag(R.id.plant_id);
                String currentPlantName = (String) sellButton.getTag(R.id.plant_name);
                String currentPlantQuantity = (String) sellButton.getTag(R.id.plant_quantity);

                if (Integer.parseInt(currentPlantQuantity) > 0) {
                    // get URI
                    Uri currentPlantUri = ContentUris.withAppendedId(PlantEntry.CONTENT_URI, Long.parseLong(currentPlantIdTag));

                    // connecting views
                    TextView currentPlantView = view.findViewById(R.id.plant_quantity);
                    String currentNewPlantQuantity = String.valueOf(Integer.parseInt(currentPlantQuantity) -1);
                    currentPlantView.setText(currentNewPlantQuantity);

                    // message for the plant being sold
                    Toast.makeText(context, currentNewPlantQuantity + context.getString(R.string.plant_sold),
                            Toast.LENGTH_SHORT).show();

                    ContentValues values = new ContentValues();
                    values.put(PlantEntry.COLUMN_PLANT_NAME, currentPlantName);
                    values.put(PlantEntry.COLUMN_PLANT_QUANTITY, currentNewPlantQuantity);

                    int rowsAffected = context.getApplicationContext().getContentResolver().update(currentPlantUri, values, null, null);

                    if (rowsAffected > 0 ){
                        changeCursor(cursor);
                        notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(context, currentPlantName + context.getString(R.string.plant_zero)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.plantName.setText(plantName);
        holder.plantPrice.setText(context.getString(R.string.price_unit) + String.valueOf(plantPrice));
        holder.plantQuantity.setText(String.valueOf(plantQuantity));

    }
}