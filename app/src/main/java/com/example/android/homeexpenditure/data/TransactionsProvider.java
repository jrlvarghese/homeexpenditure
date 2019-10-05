package com.example.android.homeexpenditure.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

// Import the data packages
import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;
import com.example.android.homeexpenditure.data.TransactionsContract;


public class TransactionsProvider extends ContentProvider
{
    // Get an instance of TransactionsDbHelper within this class
    private TransactionsDbHelper myDbHelper;
	
	// Integer constants for assigning values for particular uri
	private final static int NAMES_TABLE = 100;	
	private final static int NAMES_ROW = 101;
	
	private final static int TRANS_TABLE = 200;
	private final static int TRANS_ROW = 201;

	private final static int TRANS_WITH_NAMES = 300;
	private final static int COMBINED_ROW = 301;

	private final static int SUM_PERSON_WISE = 400;
	private final static int SUM_PERSON_WISE_ROW = 401;

	// Use a global variable for class UriMatcher
	private final static UriMatcher myUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static{
		// For the NAMES TABLE
		myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_NAMES, NAMES_TABLE);
		myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_NAMES+"/#", NAMES_ROW);
		// For the TRANSACTIONS TABLE
		myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_TRANSACTIONS, TRANS_TABLE);
		myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_TRANSACTIONS+"/#", TRANS_ROW);
		// For the COMBINED TABLE
//        myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_COMBINED, TRANS_WITH_NAMES);
        myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_COMBINED, TRANS_WITH_NAMES);
        myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_COMBINED+"/#", COMBINED_ROW);
//        // For person wise sum
//        myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_COMBINED, SUM_PERSON_WISE);
//        myUriMatcher.addURI(TransactionsContract.CONTENT_AUTHORITY, TransactionsContract.PATH_COMBINED+"/#", SUM_PERSON_WISE_ROW);
	}
	
    // Initialise the provider class and database helper class
    @Override
    public boolean onCreate()
    {
        // On creating the instance of this class -- call TransactionsDbHelper
        myDbHelper = new TransactionsDbHelper(getContext());
        return true;
    }

    // Perform the query for the given uri
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Get an instance of a readable sqlite database
        SQLiteDatabase myDb = myDbHelper.getReadableDatabase();
        // Instantiate a cursor
        Cursor cursor = null;

        // Get the match value so that a proper decision can be taken based on the incoming uri
        final int match = myUriMatcher.match(uri);

        switch (match)
        {
            case NAMES_TABLE:
                cursor = myDb.query(TransactionsEntry.NAMES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NAMES_ROW:
                selection = TransactionsEntry.COLUMN_NAME_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = myDb.query(TransactionsEntry.NAMES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANS_TABLE:
                cursor = myDb.query(TransactionsEntry.TRANS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANS_ROW:
                selection = TransactionsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = myDb.query(TransactionsEntry.TRANS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANS_WITH_NAMES:
                Log.v("inside_query_uri", uri.toString());
                cursor = myDb.query(TransactionsEntry.TRANS_AND_NAMES_TABLE, projection, selection, selectionArgs, null, null, null);
                break;
            case COMBINED_ROW:
                selection = TransactionsEntry.TRANS_TABLE+"."+TransactionsEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                Log.v("seleciton_args", selectionArgs[0]);
                cursor = myDb.query(TransactionsEntry.TRANS_AND_NAMES_TABLE, projection, selection, selectionArgs, null, null, null);
                break;
//            case SUM_PERSON_WISE:
//                cursor = myDb.query(TransactionsEntry.NAMEWISE_SUM_TABLE, projection, selection, selectionArgs, null, null, null);
//                break;
//            case SUM_PERSON_WISE_ROW:
//                projection = new String[]{
////                        TransactionsEntry.NAMES_TABLE+"."+TransactionsEntry.COLUMN_NAME_ID,
////                        TransactionsEntry.TRANS_TABLE+"."+TransactionsEntry.COLUMN_PERSON_ID,
////                        TransactionsEntry.COLUMN_NAME,
//                        "SUM("+TransactionsEntry.COLUMN_AMOUNT+")"};
//                //selection = TransactionsEntry.TRANS_TABLE+"."+TransactionsEntry.COLUMN_PERSON_ID+"=?";
//                //selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
//                groupBy = TransactionsEntry.COLUMN_NAME;
//                groupBy = null;
//                cursor = myDb.query(TransactionsEntry.TRANS_AND_NAMES_TABLE, projection, selection, selectionArgs, groupBy, null, null);
//                break;

            default:
                throw new IllegalArgumentException("Cannot query this unknown URI: \n" + uri);
		}
        // Set the notification Uri on the cursor
        // So we know what content URI the cursor was created for
        // If the data at this URI changes, then we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
//        Log.v("query_content_provider", "After querying:: notify the change");
        Log.v("view_uri", uri.toString());

        return cursor;
    }

    @Override
    public String getType(Uri uri)
	{
		final int match = myUriMatcher.match(uri);
        switch (match)
        {
            case NAMES_TABLE:
                return TransactionsEntry.CONTENT_LIST_TYPE_NAMES;
            case NAMES_ROW:
                return TransactionsEntry.CONTENT_ITEM_TYPE_NAMES;
			case TRANS_TABLE:
				return TransactionsEntry.CONTENT_LIST_TYPE;
			case TRANS_ROW:
				return TransactionsEntry.CONTENT_ITEM_TYPE;
            case TRANS_WITH_NAMES:
                return TransactionsEntry.CONTENT_LIST_TYPE_COMBINED;
            case COMBINED_ROW:
                return TransactionsEntry.CONTENT_ITEM_TYPE_COMBINED;
                /* Problem while using the following part is that when we load the independent recordings
                * there will be a problem; as both of them shares the same CONTENT_TYPE */
            case SUM_PERSON_WISE:
                return TransactionsEntry.CONTENT_LIST_TYPE;
//            case SUM_PERSON_WISE_ROW:
//                return TransactionsEntry.CONTENT_ITEM_NAMEWISE_SUM;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        // Get the match value
        final int match = myUriMatcher.match(uri);

        switch(match)
        {
            case NAMES_TABLE:
                // insertNames function is used to insert data into names table
                return insertNames(uri, contentValues);
            case TRANS_TABLE:
                // insertTrans method is used to insert data into trans table; refer below for this functions
                return insertTrans(uri, contentValues);
            default:
                throw new IllegalArgumentException("Cannot insert using unknown uri \n" + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Get a writable sqlite database
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        final int match = myUriMatcher.match(uri);

        switch(match)
        {
            case NAMES_TABLE:
                return db.delete(TransactionsEntry.NAMES_TABLE, selection, selectionArgs);
            case NAMES_ROW:
                selection = TransactionsEntry.COLUMN_NAME_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Get the number of rows deleted to a variable
                int rowsDeleted = db.delete(TransactionsEntry.NAMES_TABLE, selection, selectionArgs);
                // If a row is deleted notify a change
                if(rowsDeleted != 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return rowsDeleted;
            case TRANS_TABLE:
                return db.delete(TransactionsEntry.TRANS_TABLE, selection, selectionArgs);
            case TRANS_ROW:
                selection = TransactionsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Get the number of rows deleted to a variable
                rowsDeleted = db.delete(TransactionsEntry.TRANS_TABLE, selection, selectionArgs);
                // If a row is deleted notify a change so that the cursor loader can reload
                if(rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Cannot complete delete this process, unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        final int match = myUriMatcher.match(uri);
        switch(match)
        {
            case NAMES_TABLE:
                return updateNames(uri, contentValues, selection, selectionArgs);
            case NAMES_ROW:
                selection = TransactionsEntry.COLUMN_NAME_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateNames(uri, contentValues, selection, selectionArgs);
            case TRANS_TABLE:
                return updateTrans(uri, contentValues, selection, selectionArgs);
            case TRANS_ROW:
                selection = TransactionsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateTrans(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknow uri for updating data: " + uri);

        }
    }

    // Custom methods
    // Method to insert data into the NAMES_TABLE
    public Uri insertNames(Uri uri, ContentValues contentValues)
    {
        String personName = contentValues.getAsString(TransactionsEntry.COLUMN_NAME);
        if(personName == null)
            throw new IllegalArgumentException("Person requires a name to insert into the names table");

        // Get a writeable database using the database helper class
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        // insert into the database using the insert method
        long newRowId = db.insert(TransactionsEntry.NAMES_TABLE, null, contentValues);
        if(newRowId == -1)
            return null;
        else{
            // Notify that a change have been occured to the database
            getContext().getContentResolver().notifyChange(uri, null);
            // Return the uri with appended id of the new row
            return ContentUris.withAppendedId(uri, newRowId);

        }
    }

    public Uri insertTrans(Uri uri, ContentValues contentValues)
    {
        // Check the date; if there is no date field throw an exception
        Integer transDate = contentValues.getAsInteger(TransactionsEntry.COLUMN_DATE);
        if(transDate == null)
            throw new IllegalArgumentException("Transactions date cannot be null");

        Integer transMonth = contentValues.getAsInteger(TransactionsEntry.COLUMN_MONTH);
        if(transMonth == null)
            throw new IllegalArgumentException("Transactions month cannot be null");

        Integer transYear = contentValues.getAsInteger(TransactionsEntry.COLUMN_YEAR);
        if(transYear == null)
            throw new IllegalArgumentException("Transactions year cannot be null");

        Integer transAmount = contentValues.getAsInteger(TransactionsEntry.COLUMN_AMOUNT);
        if(transAmount == null)
            throw new IllegalArgumentException("There should be a trnasactions amount");

        Integer transPersonId = contentValues.getAsInteger(TransactionsEntry.COLUMN_PERSON_ID);
        if((transPersonId == null)||(transPersonId<1))
            throw new IllegalArgumentException("There must be a person id ");

        // After the sanity check get a writable database to write contents into the database
        SQLiteDatabase db = myDbHelper.getWritableDatabase();

        long newRowId = db.insert(TransactionsEntry.TRANS_TABLE, null, contentValues);
        if(newRowId == -1)
            return null;
        else{
//            Log.v("insert_trans", "After inserting :: notifying change");
            //Log.v("uri_after_inserting:", uri.toString() + ",names");
            // notify that there is a change in the database
            // Here ",names" is appended at the end for notifying that the change is happened for
            // the combined cursor
            getContext().getContentResolver().notifyChange(Uri.parse(uri.toString() + ",names"), null);
            // Also notify the change in transactions cursor loader
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(Uri.parse(uri.toString() + ",names"), newRowId);
        }


    }

    // Custom method for updating data
    // For updating names
    public int updateNames(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs)
    {
        if(contentValues.containsKey(TransactionsEntry.COLUMN_NAME)){
            String personName = contentValues.getAsString(TransactionsEntry.COLUMN_NAME);
            if(personName == null)
                throw new IllegalArgumentException("Requires a name, for names table. ");
        }
        // If there is nothing to update
        if(contentValues.size() == 0)
            return 0;
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TransactionsEntry.NAMES_TABLE, contentValues, selection, selectionArgs);
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    // For updating trnsactions
    public int updateTrans(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs)
    {
        // Sanity checking for date
        if(contentValues.containsKey(TransactionsEntry.COLUMN_DATE)){
            Integer transDate = contentValues.getAsInteger(TransactionsEntry.COLUMN_DATE);
            if(transDate == null)
                throw new IllegalArgumentException("Requires a dat for trnasaction.");
        }
        // Sanity checking for month
        if(contentValues.containsKey(TransactionsEntry.COLUMN_MONTH)){
            Integer transMonth = contentValues.getAsInteger(TransactionsEntry.COLUMN_MONTH);
            if(transMonth == null)
                throw new IllegalArgumentException("Requires a month for transaction.");
        }
        // Sanity checking for year
        if(contentValues.containsKey(TransactionsEntry.COLUMN_YEAR)){
            Integer transYear = contentValues.getAsInteger(TransactionsEntry.COLUMN_YEAR);
            if(transYear == null)
                throw new IllegalArgumentException("Requires a year for trnasactions.");
        }
        // Sanity checking for amount
        if(contentValues.containsKey(TransactionsEntry.COLUMN_AMOUNT)){
            Integer transAmount = contentValues.getAsInteger(TransactionsEntry.COLUMN_AMOUNT);
            if(transAmount == null)
                throw new IllegalArgumentException("Requires a value for amount.");
        }
        // Sanity checking for person id
        if(contentValues.containsKey(TransactionsEntry.COLUMN_PERSON_ID)){
            Integer transPersonId = contentValues.getAsInteger(TransactionsEntry.COLUMN_PERSON_ID);
            if(transPersonId == null)
                throw new IllegalArgumentException("Requires a person for transaction. ");
        }

        // If there is nothing to update, return zero
        if(contentValues.size() == 0)
            return 0;
        // If there is something to update, get a  database and update
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TransactionsEntry.TRANS_TABLE, contentValues, selection, selectionArgs);
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;

    }

}