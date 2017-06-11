package com.jibstream.pennyroyalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AddStockFragment extends Fragment implements View.OnClickListener {

    AutoCompleteTextView stockAutoComplete;
    EditText boughtSharesEditText;
    EditText priceBoughtEditText;
    ArrayList<String> stocks;
    SharedPreferences preferences;
    String stockCode;

    public AddStockFragment() {
        stocks = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_add_stock, container, false);
        preferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        stockAutoComplete = (AutoCompleteTextView) v.findViewById(R.id.stockCompleteTextView);
        boughtSharesEditText = (EditText) v.findViewById(R.id.sharesEditText);
        priceBoughtEditText = (EditText) v.findViewById(R.id.sharePriceEditText);
        Button addStockButton = (Button) v.findViewById(R.id.addStockButton);
        addStockButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addStockButton:
                addStock(view);
        }
    }

    public void addStock(View view) {
        String boughtShares = boughtSharesEditText.getText().toString();
        String priceBought = priceBoughtEditText.getText().toString();
        if (stockCode == null || boughtShares.matches("") || priceBought.matches("")) {
            Toast.makeText(getActivity(), "Please Fill in all the above fields", Toast.LENGTH_SHORT).show();
        } else {
            int profileId = 0;
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS buys " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "profile INTEGER NOT NULL, " +
                    "code VARCHAR NOT NULL, " +
                    "price DECIMAL(6, 4) NOT NULL, " +
                    "holdings INTEGER NOT NULL)");
            try {
                Cursor cursor =  db.rawQuery("SELECT id FROM profiles WHERE name = '" + preferences.getString("profile", "") + "' LIMIT 1", null);
                int profileIdIndex = cursor.getColumnIndex("id");
                if(cursor!=null && cursor.getCount()>0) {
                    cursor.moveToFirst();
                    do {
                        profileId = cursor.getInt(profileIdIndex);
                    } while (cursor.moveToNext());
                }

                if (profileId != 0) {
                    db.execSQL("INSERT INTO buys (profile, code, price, holdings) VALUES " +
                            "('" + profileId + "', " +
                            "'" + stockCode + "', " +
                            "'" + priceBought + "', " +
                            "'" + boughtShares + "')");
                }
                //preferences.edit().putString("profile", profileName.getText().toString()).apply();
                // Create new fragment and transaction
                //Fragment addStockFragment = new AddStockFragment();
                //FragmentManager transaction = getActivity().getSupportFragmentManager();
                //transaction.beginTransaction().replace(R.id.fragment, addStockFragment).commit();
            } catch (SQLiteConstraintException sqlException) {
                Toast.makeText(getActivity(), "This profile name already exists.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unknown error occurred.", Toast.LENGTH_SHORT).show();
                Log.i("Error", e.getMessage());
            }

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        stockAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stockCode = null;
            }

            @Override
            public void afterTextChanged(Editable s) {
                StockSearchTask task = new StockSearchTask();
                task.execute("https://www.google.ca/finance/match?matchtype=matchall&q=" + s.toString());
            }
        });

        stockAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] stockName = stocks.get(position).split(" - ");
                stockCode = stockName[0];
                //TextView companyName = (TextView)getView().findViewById(R.id.companyNameTextView);
                //companyName.setText(stockName[1]);
                //companyPrice.setText("Retreiving Price...");
                //companyPrice.setTextSize(12f);
                //CurrentStockInfo info = new CurrentStockInfo();
                //info.execute("http://www.alphavantage.co/query?apikey=PKK5&function=TIME_SERIES_INTRADAY&interval=1min&symbol=" + stockName[0]);

            }
        });
    }

    public class StockSearchTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;

                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            stocks.clear();
            try {
                JSONObject json = new JSONObject(result);
                String matches = json.getString("matches");

                JSONArray arr = new JSONArray(matches);

                int limit = 4;
                if(arr.length() < 4) {
                    limit = arr.length();
                }

                for (int i = 0; i < limit; i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String code = jsonPart.getString("t");
                    if(code.matches("")) {
                        continue;
                    }
                    stocks.add(code + " - " + jsonPart.getString("n"));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, stocks);
                stockAutoComplete.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /*public class CurrentStockInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;

                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.matches("")) {
                super.onPostExecute(result);
                try {
                    JSONObject json = new JSONObject(result);
                    String lastRefreshed = json.getJSONObject("Meta Data").getString("3. Last Refreshed");
                    double price = json.getJSONObject("Time Series (1min)").getJSONObject(lastRefreshed).getDouble("1. open");
                    //currentPrice = (float)price;
                    //companyPrice.setTextSize(24f);
                    //companyPrice.setText("$" + String.valueOf(currentPrice));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}
