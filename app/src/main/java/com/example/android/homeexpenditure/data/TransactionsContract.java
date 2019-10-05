package com.example.android.homeexpenditure.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TransactionsContract
{
    private TransactionsContract(){
        // Default constructor for the TransactionsContract class
    }

    // String constants for drugs provider class
    public final static String CONTENT_AUTHORITY = "com.example.android.homeexpenditure";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
	public final static String PATH_TRANSACTIONS = "trans";
    public final static String PATH_NAMES = "names";
    public final static String PATH_COMBINED = "trans,names";

    public static final class TransactionsEntry implements BaseColumns
    {
        // PARTICULARS OF THE TRANSACTIONS TABLE
        // Name for the database table for drugs
        public final static String TRANS_TABLE = "trans";
        // Unique id number for the drugs
        public final static String _ID = BaseColumns._ID;
        // Column names for the date
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_MONTH = "month";
        public final static String COLUMN_YEAR = "year";
        // Column name for amount
        public final static String COLUMN_AMOUNT = "amount";
        // Column name for person who paid
        public final static String COLUMN_PERSON_ID = "name_id";
        // Column name for comments
        public static final String COLUMN_COMMENTS = "comments";
        //************************************************************
        

        // PARTICULARS FOR THE NAMES TABLE
        public static final String NAMES_TABLE = "names";
        public static final String COLUMN_NAME_ID = "name_id";
        public static final String COLUMN_NAME = "person_name";

        // FOR GETTING COMBINED TABLE VALUES
        public static final String TRANS_AND_NAMES_TABLE = "trans LEFT OUTER JOIN names ON trans.name_id=names.name_id";

        // FOR GETTING NAMEWISE SUM
        public static final String NAMEWISE_SUM_TABLE = "trans GROUP BY trans.name_id";

        //public static final String NAMEWISE_SUM_TABLE = "trans LEFT "
        // FOR GETTING COMBINED TABLE VALUES
        // ****************************************************************
        // Create string containing the sql statement for creating the sql table
        public final static String SQL_CREATE_NAMES_TABLE = "CREATE TABLE " + NAMES_TABLE + " ("
                + COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL);";

		// Create a string that contains the SQL statement for creating a SQL table
        public final static String SQL_CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TRANS_TABLE + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " INTEGER NOT NULL, "
                + COLUMN_MONTH + " INTEGER NOT NULL, "
                + COLUMN_YEAR + " INTEGER NOT NULL, "
                + COLUMN_AMOUNT + " INTEGER NOT NULL, "
                + COLUMN_PERSON_ID + " INTEGER NOT NULL, "
                + COLUMN_COMMENTS + " TEXT, "
				+ "FOREIGN KEY "+"("+COLUMN_PERSON_ID+")"+" REFERENCES "+NAMES_TABLE+"("+COLUMN_NAME_ID+"));";
//        // String constants for sort order using generic name
//        public final static String GENERIC_DESCENDING = COLUMN_GENERIC_NAME + " DESC";
//        public final static String GENERIC_ASCENDING = COLUMN_GENERIC_NAME + " ASC";

        // Create a string that contains the SQL statement for deleting the SQL table
        public final static String SQL_DELETE_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS " + TransactionsEntry.TRANS_TABLE;
        public final static String SQL_DELETE_NAMES_TABLE = "DROP TABLE IF EXISTS " + TransactionsEntry.NAMES_TABLE;
        
		// Final uri to communicate using the content provider, seperate uri for transactions and names
        public final static Uri TRANS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANSACTIONS);
		public final static Uri NAMES_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NAMES);
		// Combined table uri
        public final static Uri COMBINED_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COMBINED);
        // Name wise sum uri
        //public final static Uri NAMEWISE_SUM_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANSACTIONS);

        // For the getType method in the TransactionsProvider class
        public final static String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;
        public final static String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;
		
		// 
		public final static String CONTENT_LIST_TYPE_NAMES = 
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAMES;
		public final static String CONTENT_ITEM_TYPE_NAMES = 
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAMES;

		public final static String CONTENT_LIST_TYPE_COMBINED =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMBINED;
		public final static String CONTENT_ITEM_TYPE_COMBINED =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMBINED;

//		public final static String CONTENT_LIST_NAMEWISE_SUM =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMBINED;
//		public final static String CONTENT_ITEM_NAMEWISE_SUM =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_COMBINED;
    }
}
