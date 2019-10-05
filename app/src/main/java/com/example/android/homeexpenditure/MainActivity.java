package com.example.android.homeexpenditure;

import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
//import android.support.v4.app.ActivityCompat;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.homeexpenditure.data.TransactionsContract;
import com.example.android.homeexpenditure.data.TransactionsDbHelper;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry.COMBINED_URI;
import static com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry.NAMES_URI;
import static com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry.TRANS_URI;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    // Variables for date
    private int date = 0;
    private int month = 0;
    private int year = 0;
    private String monthName = "null";

    private final String LOG_TAG = "homeexpenditure";

    // Id for the TRANSACTIONS_LOADER
    private static final int TRANSACTIONS_LOADER = 1;
    // Id for the NAMES_LOADER
    private static final int NAMES_LOADER = 2;
    // id for loading both the tables
    private static final int COMBINED_LOADER = 3;
    // id for loading TODAYS TRANSACTIONS
    private static final int TODAY_SUM_LOADER = 4;
    // id for loading name wise total
    private static final int NAMEWISE_SUM_LOADER = 5;

    // Get an instance of the cursor adapter class
    TransactionsCursorAdapter myTransCursorAdapter;
    SummaryCursorAdapter mySummaryCursorAdapter;

    // Instance of the database helper class
    private TransactionsDbHelper mTransactionDbHelper;

    // String variables for selection and selectionArgs
    private String mSelection = null;
    private String[] mSelectionArgs = new String[]{};
    private String[] mProjection = new String[]{};
    private String mSortOrder = null;


    // Get today's date month year from calendar class
    Calendar calendar = Calendar.getInstance();
    final int todayDate = calendar.get(Calendar.DATE);
    final int todayMonth = calendar.get(Calendar.MONTH);
    final int todayYear = calendar.get(Calendar.YEAR);
    final String todayMonthName = getMonthName(calendar.get(Calendar.MONTH));

    private List<String> nameListString = new ArrayList<String >();

    private Uri myCurrentTransactionsUri;
    private long selectedId = 0;

    // public static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_ID_READ_PERMISSION = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        // using the setDate function set the date in the view
//        setDate(todayDate, todayMonth, todayYear);
        date = todayDate;
        month = todayMonth;
        year = todayYear;
        monthName = todayMonthName;
        // Set the date on the view
        setDate(date, month, year);

        // Setup the response from the floating action button
        // First find the floating action button; using the resource id
        FloatingActionButton fab = findViewById(R.id.fab);
        // Set the onClick listener on the floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set an intent that have to be sent to another activity on clicking the fab
                Intent intent = new Intent(MainActivity.this, EditTransactionsActivity.class);
                intent.putExtra("add_transaction_intent", Integer.toString(2));
                intent.putExtra("date_intent", Integer.toString(date));
                intent.putExtra("month_intent", Integer.toString(month));
                intent.putExtra("year_intent", Integer.toString(year));
                startActivity(intent);
            }
        });

        mTransactionDbHelper = new TransactionsDbHelper(this);

        // Find the list view where your data have to be populated
        final ListView transactionsListView = findViewById(R.id.transactions_list);
        // Get the empty view and set the text when there is nothing to dsiplay
        View emptyView = findViewById(R.id.empty_view);
        transactionsListView.setEmptyView(emptyView);
        // Get the cursorAdapter
        myTransCursorAdapter = new TransactionsCursorAdapter(this, null);
        // Attache the cursorAdapter to the list view
        transactionsListView.setAdapter(myTransCursorAdapter);

        // Set an onClick listener for editing or deleting a particular transaction entry
        transactionsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedId = id;
                //transactionsListView.setItemChecked(position, true);
                //view.setSelected(true);
                view.setBackgroundColor(Color.parseColor("#80cbc4"));
                return true;
            }
        });


//        ListView summaryListView = findViewById(R.id.summary_list);
//        mySummaryCursorAdapter = new SummaryCursorAdapter(this, null);
//        summaryListView.setAdapter(mySummaryCursorAdapter);

        //setSummary();

        getLoaderManager().initLoader(COMBINED_LOADER, null, this);
        getLoaderManager().initLoader(TODAY_SUM_LOADER, null, this);
        getLoaderManager().initLoader(NAMES_LOADER, null, this);
//        getLoaderManager().initLoader(NAMEWISE_SUM_LOADER, null, this);
        setSummary();
//        Log.v("main_activity", nameListString.get(1));

    }


    // Create menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    // Choosing the actions on pressing the menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_transaction:
                deleteTransaction(selectedId);
                return true;
            case R.id.action_export_data:
                exportData();
                return true;
//            case R.id.action_delete_all:
//                deleteMonthData();
//                return true;
            case R.id.action_delete_names:
                deleteNames();
                return true;
			case R.id.action_insert_names:
				insertName();
				return true;
            // case R.id.action_share_via_mail:
                // shareFile();
                // return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
	

    // ------------------------ METHODS WITHIN THE LOADER CLASS --------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
    {
        switch(id){
            case COMBINED_LOADER:
                mSelection = TransactionsEntry.COLUMN_DATE + "=?"
                + " AND " + TransactionsEntry.COLUMN_MONTH + "=?"
                + " AND " + TransactionsEntry.COLUMN_YEAR + "=?";
                mSelectionArgs = new String[]{Integer.toString(date), Integer.toString(month), Integer.toString(year)};
                return new CursorLoader(this,
                        COMBINED_URI,
                        null,
                        mSelection,
                        mSelectionArgs,
                        null);
            case TODAY_SUM_LOADER:
                mProjection = new String[]{
                        "SUM("+TransactionsEntry.COLUMN_AMOUNT+")"
                };
                mSelection = TransactionsEntry.COLUMN_DATE + "=?"
                        + " AND " + TransactionsEntry.COLUMN_MONTH + "=?"
                        + " AND " + TransactionsEntry.COLUMN_YEAR + "=?";
                mSelectionArgs = new String[]{Integer.toString(date), Integer.toString(month), Integer.toString(year)};
                return new CursorLoader(this,
                        TransactionsEntry.TRANS_URI,
                        mProjection,
                        mSelection,
                        mSelectionArgs,
                        null);
//            case NAMEWISE_SUM_LOADER:
//                mProjection = new String[]{
//                        TransactionsEntry.COLUMN_PERSON_ID,
//                        "SUM("+TransactionsEntry.COLUMN_AMOUNT+")"
//                };
//
//                return new CursorLoader(this,
//                        TransactionsEntry.TRANS_URI,
//                        mProjection,
//                        null,
//                        null,
//                        null);
            case NAMES_LOADER:
                return new CursorLoader(this,
                        TransactionsEntry.NAMES_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                throw new IllegalArgumentException("Unknown loader id!!");


        }



    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        switch (loader.getId()){
            case COMBINED_LOADER:
                myTransCursorAdapter.swapCursor(cursor);
                break;
            case TODAY_SUM_LOADER:
                setTodaysTotal(cursor);
                break;
//            case NAMEWISE_SUM_LOADER:
////                mySummaryCursorAdapter.swapCursor(cursor);
//                setSummary();
//                break;
            case NAMES_LOADER:
                loadNames(cursor);
                break;
            default:
                throw new IllegalArgumentException("Unknown id for onLoaderFinished");

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        switch(loader.getId()){
            case COMBINED_LOADER:
                myTransCursorAdapter.swapCursor(null);
                break;
            case TODAY_SUM_LOADER:
                TextView sumTextView = findViewById(R.id.todays_total_text);
                sumTextView.setText(" 0");
                break;
//            case NAMEWISE_SUM_LOADER:
////                mySummaryCursorAdapter.swapCursor(null);
//                TextView summaryTextView = findViewById(R.id.summary_details);
//                summaryTextView.setText(" 0");
//                break;
            case NAMES_LOADER:
                nameListString.removeAll(nameListString);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri on loader reset");

        }

    }

    // ########################### CUSTOM METHODS #########################################

//    // method to insert drugs
//    public void insertNames()
//    {
//        ContentValues values = new ContentValues();
//        values.put(TransactionsEntry.COLUMN_NAME, "MyName");
//        Uri uri = getContentResolver().insert(NAMES_URI, values);
//        if (uri == null)
//            Toast.makeText(this, "Failed to insert data!!", Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, "Successfully inserted data!!", Toast.LENGTH_SHORT).show();
//    }
//
//    // Method to insert transactions
//    public void insertTransactions()
//    {
//        ContentValues values = new ContentValues();
//        values.put(TransactionsEntry.COLUMN_DATE, 8);
//        values.put(TransactionsEntry.COLUMN_MONTH, 11);
//        values.put(TransactionsEntry.COLUMN_YEAR, 2018);
//
//        values.put(TransactionsEntry.COLUMN_AMOUNT, 100);
//        values.put(TransactionsEntry.COLUMN_PERSON_ID, 1);
//        values.put(TransactionsEntry.COLUMN_COMMENTS, "NO COMMENTS");
//
//        Uri uri = getContentResolver().insert(TRANS_URI, values);
//        if (uri == null)
//            Toast.makeText(this, "Failed to insert transactions data!!", Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, "Successfully inserted transactions!!", Toast.LENGTH_SHORT).show();
//    }

    public void setDate(int d, int m, int y)
    {
        TextView dateText = findViewById(R.id.date);
        dateText.setText(Integer.toString(d));


        String mnth = getMonthName(m);
        TextView monthText = findViewById(R.id.month);
        monthText.setText(mnth);

        TextView yearText = findViewById(R.id.year);
        yearText.setText(Integer.toString(y));

    }

    // METHOD TO DECREMENT DATE, THIS WILL BE CALLED WHEN THE DECREMENT BUTTON IS PRESSED
    public void decrementDate(View view)
    {
        // Decrement the date when the decrement button is pressed
        date--;
        if(date<1){
            // Once the date is less than one set the date to 31
            date = 31;
            // Also decrement month as it's going backwards to the previous month
            month--;
            // Decrement month once month is less than 0, which is January
            if(month<0) {
                year--;
                month = 11;
            }
        }
        // Using the setDate method set the date in the date field
        setDate(date, month, year);
        // Reload the data
        getLoaderManager().restartLoader(COMBINED_LOADER, null, this);
        getLoaderManager().restartLoader(TODAY_SUM_LOADER, null, this);
    }

    // METHOD TO INCREMENT DATE SO AS TO NAVIGATE TO DIFFERENT DATES, WILL BE CALLED WHEN INCREMENT BUTTON IS PRESSED
    public void incrementDate(View view)
    {
        // Increment the date on every press on the increment button
        date++;
        if(date>31){
            // Once date is greater than 31 set it to 1 and increment the month
            date = 1;
            month++;
            if(month>11){
                // If the month goes beyond 11, set month to 0 and also increment year
                month = 0;
                year++;
            }
        }
        // Set the new date in the display using the setDate method
        setDate(date, month, year);
        getLoaderManager().restartLoader(COMBINED_LOADER, null, this);
        getLoaderManager().restartLoader(TODAY_SUM_LOADER, null, this);
    }

    // METHOD TO FIND THE TOTAL EXPENDITURE DONE ON A SINGLE DATE, FOR THIS THE CURSOR IS
    // LOADED USING LOADER MANAGER AND THE CURSOR IS PASSED TO THIS METHOD
    public void setTodaysTotal(Cursor cursor)
    {
        float totalAmount = 0;
        try{
            // As the cursor will contain only one row corresponding to a date
            cursor.moveToFirst();
            int sumColumnIndex = cursor.getColumnIndex("SUM(" + TransactionsEntry.COLUMN_AMOUNT + ")");
            Log.v("Cursor index for sum", Integer.toString(sumColumnIndex));
            totalAmount = ((float)cursor.getInt(sumColumnIndex))/100;
        }catch (Exception ex){
            Log.e("Cursor for getting sum", ex.toString());
        }
        TextView sumTextView = findViewById(R.id.todays_total_text);
        sumTextView.setText("\u20B9 "+ Float.toString(totalAmount));

    }


    // METHOD TO SET SUMMARY AT THE END OF TODAYS TRANSACTION LIST
    public void setSummary()
    {
		// Get a readable database using the database helper class
        SQLiteDatabase db = mTransactionDbHelper.getReadableDatabase();
		// Using the projection, get required fields, including the sum of all transactions grouped by the personal id
        String[] projection = new String[]{
                TransactionsEntry.TRANS_TABLE + "." +TransactionsEntry.COLUMN_PERSON_ID,
                TransactionsEntry.COLUMN_NAME,
                TransactionsEntry.COLUMN_MONTH,
                "SUM("+TransactionsEntry.COLUMN_AMOUNT+")"
        };
		// Select transactions for the present month 
        String selection = TransactionsEntry.COLUMN_MONTH + "=?";
        String[] selectionArgs = new String[]{Integer.toString(month)};
		// GroupBy the person id
        String groupBy = TransactionsEntry.TRANS_TABLE + "." + TransactionsEntry.COLUMN_PERSON_ID;

		// Get a cursor object using the query method in the provider class
        Cursor cursor = db.query(
                TransactionsEntry.TRANS_AND_NAMES_TABLE,
                projection,
                selection,
                selectionArgs,
                groupBy,
                null,
                null
                );
        int total = 0;
        float individualAmount = 0;
        float average = 0;
        float nett = 0;
        int x = 0;
        int nameId = 0;
        List<Integer> expense = new ArrayList<Integer>();
        //List<String> namesList = new ArrayList<String>();

        int amount = 0;

        String summaryString = "";
        String name = "";
        String temp = null;

        String summaryNameString = "";
        String summaryTotalString = "";
        String summaryNettString = "";

        if(cursor!=null){
            try{
                while(cursor.moveToNext())
                {
                    Log.v("check_cursor", "Moving to next");
                    int sumColumnIndex = cursor.getColumnIndex("SUM(" + TransactionsEntry.COLUMN_AMOUNT + ")");
                    int nameIdColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME);

                    amount = cursor.getInt(sumColumnIndex);
                    name = cursor.getString(nameIdColumnIndex);
                    expense.add(amount);
                    //namesList.add(name);

                    summaryNameString += name + "\n\n";
					// As the amount is inserted as integers by multiplying with 100; while retrieving divide by 100
                    summaryTotalString += Float.toString(((float)amount)/100) + "\n\n";
                }

            }catch (Exception ex){
                Log.e("Cursor for summary", ex.toString());
            }finally{
                cursor.close();
            }

        }else{
            summaryString = "There is nothing to display!!";
        }


        int totalPerson = cursor.getCount();
        Log.v("check_cursor", "Total persons: " + Integer.toString(totalPerson));
        // Calculate the average expenditure
        for(int i=0;i<expense.size();i++){
            total += expense.get(i);
        }
        average = ((float)total)/(totalPerson * 100);
		
		DecimalFormat decimalFormat = new DecimalFormat("#.00");
        // Calculate nett individual expenditure
        for(int i=0;i<expense.size();i++){
            nett = average - ((float)expense.get(i))/100;
            summaryNettString += decimalFormat.format(nett) + "\n\n";
        }

        TextView summaryTotalExpenseText = findViewById(R.id.summary_total_expenditure);
        summaryTotalExpenseText.setText("Total expenditure: \u20B9 "+ decimalFormat.format((float)total/100));

        TextView summaryNameText = findViewById(R.id.summary_name);
        summaryNameText.setText(summaryNameString);

        TextView summaryTotalText = findViewById(R.id.summary_total);
        summaryTotalText.setText(summaryTotalString);

        TextView summaryNettText = findViewById(R.id.summary_nett);
        summaryNettText.setText(summaryNettString);

    }

    public void loadNames(Cursor cursor)
    {
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
                //Log.v("nameListString", Integer.toString(id) + nameListString.get(id));
            }
        }catch(Exception ex){
            Log.e("cursor", ex.getMessage());
        }
    }

    // METHOD TO GET MONTH NAME AS A STRING, INPUT PARAMETER PASSED IS THE INTEGER VALUE OF THE MONTH
    public String getMonthName(int m)
    {
        String month = "wrong input in getMonthName";
        // Use the class DateFormatSymbols to get the month string
        DateFormatSymbols dfs = new DateFormatSymbols();
        // getMonths will return an array of strings of months
        String[] months = dfs.getMonths();
        if (m >= 0 && m <= 11 ) {
            month = months[m];
        }
        return month;
    }

    // INORDER TO RELOAD SUMMARY AS IT'S NOT RELOADED ON EVERY CHANGE IN THE DATABASE
    public void reloadSummary(View view)
    {
        setSummary();
    }

    // FOR DELETING TRANSACTIONS, THIS METHOD IS CALLED WHEN A MONTH IS SELECTED USING THE LONG PRESS ACTION
    public void deleteTransaction(long id)
    {
        myCurrentTransactionsUri = ContentUris.withAppendedId(TRANS_URI, id);
        if(myCurrentTransactionsUri != null){
            int rowId = getContentResolver().delete(myCurrentTransactionsUri, null, null);
            if(rowId==0)
                Toast.makeText(this, "Unable to delete this transaction!!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Transaction successfully deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    // DELETING ENTRIES OF A PARTICULAR MONTH
    public void deleteMonthData()
    {
        String selection = TransactionsEntry.COLUMN_MONTH + "=?";
        String []selectionArgs = new String[]{"10"};
        int rowsDeleted = getContentResolver().delete(TRANS_URI, selection, selectionArgs);
        if(rowsDeleted == 0)
            Toast.makeText(this, "There is nothing to delete for " + getMonthName(10), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Deleted " + Integer.toString(rowsDeleted) + " entries.", Toast.LENGTH_LONG).show();
    }

    // Method to delete names; where name entry is "MyName"
    // Provision to delete any of the names have to be added later
    public void deleteNames()
    {
        String selection = TransactionsEntry.COLUMN_NAME + "=?";
        String []selectionArgs = new String[]{"MyName"};
        int rowsDeleted = getContentResolver().delete(NAMES_URI, selection, selectionArgs);
        if(rowsDeleted == 0)
            Toast.makeText(this, "There is no name entry with MyName", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Successfully deleted name entries with MyName", Toast.LENGTH_SHORT).show();
    }
    // FOR EXPORTING DATA, AND SAVING IN AN EXCEL FILE
    public void exportData()
    {
        // Ask for permission to access the files only when the android version is more than 23
        if(Build.VERSION.SDK_INT > 23){
            // Check if the permission is already granted or not
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                // If permission is already granted then write into the file
                writeFile();
            }
            else{
                // If there is no permission then ask for the permission,
                // Before asking for the permission, should show why there is a need of permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"Need permission to write", Toast.LENGTH_LONG).show();
                }
                // Request permissions to write into the external storage
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ID_WRITE_PERMISSION);
            }

        }
        else{
            // If build version is less than 23 then there is no need of permission
            writeFile();
        }
    }

    // When you have the request results; handle the results
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // Handle the the result according to the output result
        if(requestCode == REQUEST_ID_WRITE_PERMISSION){
            // If the permission is granted then write the file
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                writeFile();
            }
            else{
                Toast.makeText(this,"No permission to write to your phone memory!", Toast.LENGTH_LONG).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }


    // FOR WRITING DATABASE CONTENT INTO AN EXCEL FILE
    public void writeFile()
    {
		String selection = TransactionsEntry.COLUMN_MONTH + "=?";
		String[] selectionArgs = new String[]{Integer.toString(month)};
        String[] projection = {
                TransactionsEntry.COLUMN_DATE,
                TransactionsEntry.COLUMN_MONTH,
                TransactionsEntry.COLUMN_YEAR,
                TransactionsEntry.COLUMN_PERSON_ID,
                TransactionsEntry.COLUMN_AMOUNT,
                TransactionsEntry.COLUMN_COMMENTS
        };

        Cursor cursor = getContentResolver().query(TRANS_URI, projection, selection, selectionArgs, TransactionsEntry.COLUMN_DATE + " ASC");

        // Get the directory where the file have to be stored
        File docDir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
        String fileName = monthName + Integer.toString(year) +".xls";
        String contents = "Hello world!\n";
        // getFileDir() returned:: /data/user/0/com.example.android.drugs_directory/files
        // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) returned :: /storage/emulated/0/Documents
        //Log.v(LOG_TAG, String.valueOf(docDir));
        // ==> /storage/emulated/0/note.txt
        String path = docDir.getAbsolutePath() + "/" + fileName;


        try {
            File file = new File(path);
            OutputStream os = new FileOutputStream(file);

            if (cursor.moveToFirst()) {
                do {
                    Log.v(LOG_TAG, "WRITING TO FILE....");
                    int dateColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_DATE);
                    int monthColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_MONTH);
                    int yearColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_YEAR);
                    int personColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_PERSON_ID);
                    int amountColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_AMOUNT);
                    int commentColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_COMMENTS);

                    int date_ = cursor.getInt(dateColumnIndex);
                    int month_ = cursor.getInt(monthColumnIndex);
                    int year_ = cursor.getInt(yearColumnIndex);
                    int personId = cursor.getInt(personColumnIndex);
                    int amount = cursor.getInt(amountColumnIndex);
                    String comment = cursor.getString(commentColumnIndex);

                    contents = Integer.toString(date_) + "/" + Integer.toString(month_ + 1) + "/" + Integer.toString(year_) + "\t";
                    int i=0;
                    for(i=1; i<personId; i++){
                        contents += "\t";
                    }
                    contents += Float.toString((float)amount/100);

                    for(int j=3; j>personId; j--){
                        contents += "\t";
                    }
                    contents += "\t" + comment + "\n";

                    os.write(contents.getBytes());

                    // do what you need with the cursor here
                } while (cursor.moveToNext());
                os.close();
            }

            Log.v(LOG_TAG, "COMPLETED WRITING....");
            //Toast.makeText(this, "Completed exporting data", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "File saved in \n" + path, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.v(LOG_TAG, e.getMessage());
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }

    public void shareFile()
    {
        // Make an intent to send file
        // Get the directory where the file have to be stored
        File docDir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
        String fileName = monthName + Integer.toString(year) +".xls";
        // Make an intent for email
        Intent sendEmail = new Intent(Intent.ACTION_SEND);
        sendEmail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/" + docDir + "/" + fileName));
        startActivity(Intent.createChooser(sendEmail, "Email: Text File"));
    }

    public void insertName()
    {
        Intent intent = new Intent(MainActivity.this, EditTransactionsActivity.class);
        intent.putExtra("add_transaction_intent", Integer.toString(3));
        intent.putExtra("date_intent", Integer.toString(date));
        intent.putExtra("month_intent", Integer.toString(month));
        intent.putExtra("year_intent", Integer.toString(year));
        startActivity(intent);

    }

}
