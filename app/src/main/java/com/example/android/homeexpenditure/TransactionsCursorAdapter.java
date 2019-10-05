package com.example.android.homeexpenditure;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.homeexpenditure.data.TransactionsContract.TransactionsEntry;
public class TransactionsCursorAdapter extends CursorAdapter
{
    // Default constructor for the TransactionsCursorAdapter
    public TransactionsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
    {
        return LayoutInflater.from(context).inflate(R.layout.transactions_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        int dateColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_DATE);
        int monthColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_MONTH);
        int yearColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_YEAR);
        int amountColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_AMOUNT);
        int commentsColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_COMMENTS);
        int personIdColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_PERSON_ID);
        int nameColumnIndex = cursor.getColumnIndex(TransactionsEntry.COLUMN_NAME);

//        String transactions_final_string = Integer.toString(cursor.getInt(dateColumnIndex)) + "-";
//        transactions_final_string += Integer.toString(cursor.getInt(monthColumnIndex)) + "-";
//        transactions_final_string += Integer.toString(cursor.getInt(yearColumnIndex)) + ":: ";
//        transactions_final_string += Float.toString(((float)cursor.getInt(amountColumnIndex))/100) + ",";
//        transactions_final_string += cursor.getString(commentsColumnIndex) + ", Name Id: ";
//        transactions_final_string += Integer.toString(cursor.getInt(personIdColumnIndex)) + ":: ";
//        transactions_final_string += cursor.getString(nameColumnIndex);

        TextView transactionsListAmountView = view.findViewById(R.id.transaction_amount);
        transactionsListAmountView.setText("\u20B9 " + Float.toString(((float)cursor.getInt(amountColumnIndex))/100));

        TextView transactionsListPaidByView = view.findViewById(R.id.transaction_paid_by);
        transactionsListPaidByView.setText("Paid by " + cursor.getString(nameColumnIndex));

        TextView transactionsListPaidForView = view.findViewById(R.id.transaction_paid_for);
        transactionsListPaidForView.setText("Paid for " + cursor.getString(commentsColumnIndex));
    }
}
