package com.example.cheesepuff.plantinventory2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import com.example.cheesepuff.plantinventory2.data.PlantContract.PlantEntry;

import org.w3c.dom.Text;

/**
 * Allows user to create a new plant or edit an existing one.
 */
public class EditorActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // EditText field to enter info
    private EditText mPlantNameEditText;
    private EditText mPlantPriceEditText;
    private EditText mPlantQuantityEditText;
    private EditText mPlantSupplierEditText;
    private EditText mPlantSupplierPhoneNumberEditText;
    private ImageButton mQuantityBtnAdd;
    private ImageButton mQuantityBtnMinus;
    private Button contactButton;

    private Uri mCurrentPlantUri;
    private static final int EXISTING_PLANT_LOADER = 1;


    private boolean mPlantHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPlantHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPlantUri = intent.getData();

        if (mCurrentPlantUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_plant));
            invalidateOptionsMenu();

        } else {
            setTitle(getString(R.string.editor_activity_title_edit_plant));
            getLoaderManager().initLoader(EXISTING_PLANT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mPlantNameEditText = (EditText) findViewById(R.id.edit_plant_name);
        mPlantPriceEditText = (EditText) findViewById(R.id.edit_plant_price);
        mPlantQuantityEditText = (EditText) findViewById(R.id.edit_plant_quantity);
        mPlantSupplierEditText = (EditText) findViewById(R.id.edit_plant_supplier);
        mPlantSupplierPhoneNumberEditText = (EditText) findViewById(R.id.edit_plant_supplier_phone);
        contactButton = findViewById(R.id.contact_button);


        mQuantityBtnAdd = findViewById(R.id.edit_plant_quantity_btn_add);
        mQuantityBtnMinus = findViewById(R.id.edit_plant_quantity_btn_minus);

        mQuantityBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("dp","clicked add");
                int currentPlantQuantity = Integer.parseInt(mPlantQuantityEditText.getText().toString());
                mPlantQuantityEditText.setText(String.valueOf(currentPlantQuantity + 1));
            }
        });

        mQuantityBtnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("dp","clicked minus");
                int currentPlantQuantity = Integer.parseInt(mPlantQuantityEditText.getText().toString());
                if (currentPlantQuantity > 0) {
                    mPlantQuantityEditText.setText(String.valueOf(currentPlantQuantity - 1));
                }
            }
        });

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = mPlantSupplierPhoneNumberEditText.getText().toString().trim();
                Intent contactIntent = new Intent(Intent.ACTION_DIAL);
                if (!TextUtils.isEmpty(number)) {
                    contactIntent.setData(Uri.parse(getString(R.string.contact_uri) + number));
                    startActivity(contactIntent);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.contact_error),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
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
                mCurrentPlantUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentPlantUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                savePlant();
                return true;

            case R.id.action_delete:
                // show dialog before delete click
                showDeleteConfirmationDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // check if field changed, if not go back to parent activity
                if (!mPlantHasChanged) {
                    // back to parent activity
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            // Setup OnTouchListeners on all the input fields, so we can determine if the user
            // has touched or modified them. This will let us know if there are unsaved changes
            // or not, if the user tries to leave the editor without saving.
            mPlantNameEditText.setOnTouchListener(mTouchListener);
            mPlantPriceEditText.setOnTouchListener(mTouchListener);
            mPlantQuantityEditText.setOnTouchListener(mTouchListener);
            mPlantSupplierEditText.setOnTouchListener(mTouchListener);
            mPlantSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
            mQuantityBtnAdd.setOnTouchListener(mTouchListener);
            mQuantityBtnMinus.setOnTouchListener(mTouchListener);

            // column indexes
            int nameColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER);

            // get data from columnIndex
            String name = cursor.getString(nameColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int supplierPhoneNumber = cursor.getInt(supplierPhoneNumberColumnIndex);

            // set data view
            mPlantNameEditText.setText(name);
            mPlantPriceEditText.setText(String.valueOf(price));
            mPlantQuantityEditText.setText(String.valueOf(quantity));
            mPlantSupplierEditText.setText(supplier);
            mPlantSupplierPhoneNumberEditText.setText(String.valueOf(supplierPhoneNumber));

        }

    }

    @Override
    public void onBackPressed() {
        // no change, move forward
        if (!mPlantHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPlantNameEditText.setText("");
        mPlantPriceEditText.setText("");
        mPlantQuantityEditText.setText("");
        mPlantSupplierEditText.setText("");
        mPlantSupplierPhoneNumberEditText.setText("");

    }

    private void savePlant() {

        // read the input fields
        // use trim to eliminate leading or trailing while space
        String plantNameString = mPlantNameEditText.getText().toString().trim();
        String plantPriceString = mPlantPriceEditText.getText().toString().trim();
        String plantQuantityString = mPlantQuantityEditText.getText().toString().trim();
        String plantSupplierString = mPlantSupplierEditText.getText().toString().trim();
        String plantSupplierPhoneNumberString = mPlantSupplierPhoneNumberEditText.getText().toString().trim();

        // in add / edit mode: field are blank, have user enter info
        if (TextUtils.isEmpty(plantNameString)
                && TextUtils.isEmpty(plantPriceString)
                && TextUtils.isEmpty(plantQuantityString)
                && TextUtils.isEmpty(plantSupplierString)
                && TextUtils.isEmpty(plantSupplierPhoneNumberString)) {

            Toast.makeText(this, getString(R.string.editor_all_fields_required),
                    Toast.LENGTH_SHORT).show();
        } else {

            Double plantPrice = 0.00;
            if (!TextUtils.isEmpty(plantPriceString)) {
                plantPrice = Double.parseDouble(plantPriceString);
            }

            int plantQuantity = 0;
            if (!TextUtils.isEmpty(plantQuantityString)) {
                plantQuantity = Integer.parseInt(plantQuantityString);
            }

            // Create a ContentValues object where column names are the keys,
            // and plants each attributes are the values.
            ContentValues values = new ContentValues();
            values.put(PlantEntry.COLUMN_PLANT_NAME, plantNameString);
            values.put(PlantEntry.COLUMN_PLANT_PRICE, plantPrice);
            values.put(PlantEntry.COLUMN_PLANT_QUANTITY, plantQuantity);
            values.put(PlantEntry.COLUMN_PLANT_SUPPLIER, plantSupplierString);
            values.put(PlantEntry.COLUMN_PLANT_SUPPLIER_PHONE_NUMBER, plantSupplierPhoneNumberString);

            if (mCurrentPlantUri == null) {
                Uri newUri = getContentResolver().insert(PlantEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_plant_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_plant_successful),
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
            } else {
                int rowsAffected = getContentResolver().update(mCurrentPlantUri, values,
                        null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.editor_update_plant_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_plant_successful),
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        }
    }

    // discardButtonClickListener is the click listener for what to do when the user confirms they
    // want to discard the changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // create alertDialog builder to set message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //create and show alertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // prompt the user to confirm that they want to delete this plant
    private void showDeleteConfirmationDialog() {
        // create and show alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // to delete
                deletePlant();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the "cancel" button, dismiss the dialog/ continue editing the plant
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePlant() {
        // only perform the delete if this is an existing plant entry
        if (mCurrentPlantUri != null) {

            /** call the ContentResolver to delete the plant at the given content URI,
             * pass in null for the selection and selection args because of the mCurrentPlantUri
             * content URI already identifies the plant that we want
             */
            int rowsDeleted = getContentResolver().delete(mCurrentPlantUri, null, null);
            // show a toast message depending on wheather or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_plant_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_plant_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}



