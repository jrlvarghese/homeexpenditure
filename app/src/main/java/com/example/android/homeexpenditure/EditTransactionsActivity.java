package com.example.android.homeexpenditure;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.homeexpenditure.data.TransactionsContract;
import com.example.android.homeexpenditure.data.TransactionsDbHelper;

import static com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry.NAMES_URI;
import static com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry.TRANS_URI;

import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class EditTransactionsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    // Instantiating of database helper class inorder to get the access to the database
    private TransactionsDbHelper myDbHelper;

    // Find all the edit fields within this activity
    // For the transactions field
    private EditText mEditComments;
    private EditText mEditAmount;
    private Spinner mNamesSpinner;
    // For the names field
    private EditText mEditNames;
    private Button mSaveNameButton;

    private String[] nameStringArray = new String[]{};
    private List<String> nameListString;

    // Variable to store and get the name id
    private int mNameId = 0;


    Cursor myCursor;

    // Integer constant which will be used by the loader class to define what will be loaded
    private static final int NAMES_LOADER = 0;

    private int date = 0;
    private int month = 0;
    private int year = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transactions);

        // Initialise a new intent and get an intent from the previous activity
        Intent intent = getIntent();
        int intentAction = Integer.parseInt(intent.getExtras().getString("add_transaction_intent"));
        date = Integer.parseInt(intent.getExtras().getString("date_intent"));
        month = Integer.parseInt(intent.getExtras().getString("month_intent"));
        year = Integer.parseInt(intent.getExtras().getString("year_intent"));

        String dateString = Integer.toString(date) + "-" + Integer.toString(month) + "-" + Integer.toString(year);

        // Find the dateTextTV in the layout and set the date
        TextView dateTextTV = findViewById(R.id.date_editor_tv);
        dateTextTV.setText(dateString);

        if(intentAction == 2){
            mEditNames = findViewById(R.id.edit_name);
            mSaveNameButton = findViewById(R.id.save_name_button);
            mEditNames.setVisibility(View.GONE);
            mSaveNameButton.setVisibility(View.GONE);
            setTitle("Add transaction");
			// To access our database, we instantiate our subclass of SQLiteOpenHelper
			// and pass the context, which is the current activity.
			myDbHelper = new TransactionsDbHelper(this);

			// Add the first element within the nameListString as "Paid by"  so that this will be displayed
			nameListString = new ArrayList<>();
			nameListString.add(null);
			nameListString.set(0, "Paid by");

			// Setting up the button click listener for saving the transactions
			Button saveTransButton = findViewById(R.id.trans_save_button);
			saveTransButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view)
				{
					saveTransactions();
					// After saving the transactions finish activity
					finish();
				}
			});

			

			// Start the loader manager
			getLoaderManager().initLoader(NAMES_LOADER, null, this);

			/* This part of the code is to setup a touch listener so that, whenever a change is made on
			* edit fields and if the user presses back button without saving the data, system will notify
			* that there is a change in the editFields which you have not saved*/
	//        // Get all the edit fields
	//        // For the transactions field
			mEditComments = findViewById(R.id.edit_comments);
			mEditAmount = findViewById(R.id.edit_amount);
			mNamesSpinner = findViewById(R.id.edit_name_spinner);

	//        // For the names fields
	//        mEditNames = findViewById(R.id.edit_name);
			setupSpinner();
        }
		else{
			setTitle("Add new person");
			// Make all the transactions field out from the view
			mEditComments = findViewById(R.id.edit_comments);
			mEditComments.setVisibility(View.GONE);
			mEditAmount = findViewById(R.id.edit_amount);
			mEditAmount.setVisibility(View.GONE);
			mNamesSpinner = findViewById(R.id.edit_name_spinner);
			mNamesSpinner.setVisibility(View.GONE);
			Button saveTransButton = findViewById(R.id.trans_save_button);
			saveTransButton.setVisibility(View.GONE);

            mEditNames = findViewById(R.id.edit_name);
			Button saveNameButton = findViewById(R.id.save_name_button);
			saveNameButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view)
				{
					saveNames();
					finish();
				}
			});
			
		}
		

        
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        getMenuInflater().inflate(R.menu.editor_activity_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem menuItem)
//    {
//        switch(menuItem.getItemId())
//        {
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(EditTransactionsActivity.this);
//                return true;
//        }
//
//        return super.onOptionsItemSelected(menuItem);
//    }

    // ------------------ Methods within the LoaderManager class -----------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection = {
                TransactionsEntry.COLUMN_NAME_ID,
                TransactionsEntry.COLUMN_NAME
        };

        return new CursorLoader(this,
                TransactionsEntry.NAMES_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
//        int cursorCount = cursor.getCount();
//        Log.v("cursor_count", Integer.toString(cursorCount));
//        cursor.moveToFirst();
//        int columnIdIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME_ID);
//        int columnNameIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME);
//        int id = cursor.getInt(columnIdIndex);
//        nameListString.add(null);
//        nameListString.set(id, cursor.getString(columnNameIndex));
        int columnIdIndex = 0;
        int columnNameIndex = 0;
        int id = 0;
        try{
            while(cursor.moveToNext())
            {
                columnIdIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME_ID);
                columnNameIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME);
                id = cursor.getInt(columnIdIndex);
                nameListString.add(null);
                nameListString.set(id, cursor.getString(columnNameIndex));
            }
        }catch(Exception ex){
            Log.e("cursor", ex.getMessage());
        }finally{
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        nameListString.removeAll(nameListString);
    }

    // ################### CUSTOM METHODS ###########################3
    // Method to insert transactions
    public void saveTransactions()
    {

        // Read from the input fields
        // As the comments are saved as strings itself get as strings itself
        String comments = mEditComments.getText().toString().trim();
        // As amount is saved as integer; first have to get as strings and later convert to integer
        String amountString = mEditAmount.getText().toString().trim();
        // amount is multiplied by 100 as all the floating point values of paisa will be converted
        float amount = 0;
        if(!TextUtils.isEmpty(amountString)){
            amount = Float.parseFloat(amountString)*100;
            amount = (int)amount;
            //Log.v("amount_entry", String.valueOf(amount));
        }

        // If there is no new entry please return without saving any blank fields
        if((TextUtils.isEmpty(amountString))){
            return;
        }

        // Have to read from the expandable list view also;
        /* HAVE TO DO THIS LATER */

//        // Get the date, month, year  using the Calendar class
//        Calendar c = Calendar.getInstance();
//        int date = c.get(Calendar.DATE);
//        int month = c.get(Calendar.MONTH);  // Month is incremented by one, as it is indexed from 0 - 11
//        int year = c.get(Calendar.YEAR);

        //Log.v("spinner_selection", Integer.toString(mNameId));
        ContentValues values = new ContentValues();
        values.put(TransactionsEntry.COLUMN_DATE, date);
        values.put(TransactionsEntry.COLUMN_MONTH, month);
        values.put(TransactionsEntry.COLUMN_YEAR, year);

        values.put(TransactionsEntry.COLUMN_AMOUNT, amount);
        values.put(TransactionsEntry.COLUMN_PERSON_ID, mNameId);
        values.put(TransactionsEntry.COLUMN_COMMENTS, comments);

        Uri uri = getContentResolver().insert(TRANS_URI, values);
        if(uri == null)
            Toast.makeText(this, "Failed to insert transactions data!!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Successfully inserted transactions!!", Toast.LENGTH_SHORT).show();

    }

    // Method to save names to the names table
    public void saveNames()
    {
        // Get the names from Edit text field, convert to string and trim if unnecessary space at the end
        String nameString = mEditNames.getText().toString().trim();
        if(TextUtils.isEmpty(nameString)){
            return;
        }
        // Add the entries to the content values and insert
        ContentValues values = new ContentValues();
        // Enter the nameString to the content values
        values.put(TransactionsEntry.COLUMN_NAME, nameString);
        Uri uri = getContentResolver().insert(NAMES_URI, values);
        if(uri == null)
            Toast.makeText(this,"Failed to insert data!!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Successfully inserted data!!", Toast.LENGTH_SHORT).show();
    }

    // Method to setup a spinner
    private void setupSpinner()
    {

        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
//        ArrayAdapter namesSpinnerAdapter = ArrayAdapter.createFromResource(this,
//                nameListString, android.R.layout.simple_spinner_item);
        ArrayAdapter<String> namesSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nameListString);

        // Specify dropdown layout style - simple list view with 1 item per line
        namesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply adapter to the spinner
        mNamesSpinner.setAdapter(namesSpinnerAdapter);
        //Get the selection values from the spinner
        mNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                mNameId = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                mNameId = 0;
            }
        });

    }


}
