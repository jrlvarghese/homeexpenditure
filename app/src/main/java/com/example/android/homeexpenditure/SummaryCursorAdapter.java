package com.example.android.homeexpenditure;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;

public class SummaryCursorAdapter extends CursorAdapter
{
    public SummaryCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
    {
        return LayoutInflater.from(context).inflate(R.layout.summary_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView summaryTextView = view.findViewById(R.id.summary_list_element);

        //int columnNameIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME);
        int columnTotalIndex = cursor.getColumnIndex(" SUM("+TransactionsEntry.COLUMN_AMOUNT+") ");
        //Log.v("bindview",Integer.toString(columnNameIndex));

        float total = ((float)cursor.getInt(columnTotalIndex))/100;
//        String name = cursor.getString(columnNameIndex);

        summaryTextView.setText( ": " + Float.toString(total));

    }
}
