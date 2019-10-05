package com.example.android.homeexpenditure.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;

public class TransactionsDbHelper extends SQLiteOpenHelper
{

    private final static String DATABASE_NAME = "transactions.db";
    private final static int DATABASE_VERSION = 1;
    // Default constructor for the DrugsDbHelper class
    public TransactionsDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db)
    {
		db.execSQL(TransactionsEntry.SQL_CREATE_NAMES_TABLE);
        db.execSQL(TransactionsEntry.SQL_CREATE_TRANSACTIONS_TABLE);
    }

    // onUpgrade Method is called when the database version updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Database at version 1 nothing to be done here
    }

    // Other methods that are inherited are
    // getReadableDatabase()
    // getWritableDatabase()
}
