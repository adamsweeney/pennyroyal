package com.jibstream.pennyroyalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
import android.widget.ExpandableListView;
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
import java.util.HashMap;
import java.util.List;

public class AddStockFragment extends Fragment implements View.OnClickListener {

    StockInfo selectedStock;
    ArrayList<StockInfo> stocks;
    SharedPreferences preferences;
    int profileId;
    List<String> boughtStockList;

    AutoCompleteTextView stockAutoComplete;
    EditText boughtSharesEditText;
    EditText priceBoughtEditText;

    ExpandableListView previousBuysListView;
    ExpandableListAdapter listAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listHash;


    public AddStockFragment() {
        stocks = new ArrayList<>();
        profileId = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_add_stock, container, false);
        boughtStockList = new ArrayList<>();
        preferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        stockAutoComplete = (AutoCompleteTextView) v.findViewById(R.id.stockCompleteTextView);
        boughtSharesEditText = (EditText) v.findViewById(R.id.sharesEditText);
        priceBoughtEditText = (EditText) v.findViewById(R.id.sharePriceEditText);
        Button addStockButton = (Button) v.findViewById(R.id.addStockButton);
        previousBuysListView = (ExpandableListView) v.findViewById(R.id.previousBuysListView);
        addStockButton.setOnClickListener(this);
        createBuysTable();
        populatePreviousBoughtStock();
        getActivity().setTitle("Add Stock");
        //SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
        //db.execSQL("DROP TABLE IF EXISTS buys");

        return v;
    }

    private void createBuysTable() {
        SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS buys " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "profile INTEGER NOT NULL, " +
                "code VARCHAR NOT NULL, " +
                "name VARCHAR NOT NULL, " +
                "exchange VARCHAR NOT NULL, " +
                "price DECIMAL(6, 4) NOT NULL, " +
                "holdings INTEGER NOT NULL, " +
                "commission DECIMAL(6, 4))");
        db.close();
    }

    private void populatePreviousBoughtStock() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listDataHeader.add("Bought Stock");

        SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
        Cursor cursor =  db.rawQuery("SELECT id FROM profiles WHERE name = '" + preferences.getString("profile", "") + "' LIMIT 1", null);
        int profileIdIndex = cursor.getColumnIndex("id");
        if(cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                profileId = cursor.getInt(profileIdIndex);
            } while (cursor.moveToNext());
        }
        cursor =  db.rawQuery("SELECT * FROM buys WHERE profile = " + profileId, null);
        int codeIndex = cursor.getColumnIndex("code");
        int priceIndex = cursor.getColumnIndex("price");
        int holdingsIndex = cursor.getColumnIndex("holdings");
        if(cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                String code = cursor.getString(codeIndex);
                String price = String.valueOf(cursor.getDouble(priceIndex));
                String holdings = String.valueOf(cursor.getInt(holdingsIndex));
                boughtStockList.add(formatBoughtStockText(code, price, holdings));
            } while (cursor.moveToNext());
        }
        listHash.put(listDataHeader.get(0), boughtStockList);
        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listHash);
        previousBuysListView.setAdapter(listAdapter);
        if(boughtStockList.size() == 0) {
            previousBuysListView.setVisibility(View.INVISIBLE);
        }
    }

    private String formatBoughtStockText(String code, String price, String holdings) {
        return code + ": " + holdings + " shares @ $" + price;
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
        if (selectedStock == null || boughtShares.matches("") || priceBought.matches("")) {
            Toast.makeText(getActivity(), "Please Fill in all the above fields", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
            try {
                if (profileId != 0) {
                    db.execSQL("INSERT INTO buys (profile, code, name, exchange, price, holdings) VALUES " +
                            "('" + profileId + "', " +
                            "'" + selectedStock.code + "', " +
                            "'" + selectedStock.name + "', " +
                            "'" + selectedStock.exchange + "', " +
                            "'" + priceBought + "', " +
                            "'" + boughtShares + "')");
                }
                boughtStockList.add(formatBoughtStockText(selectedStock.code, priceBought, boughtShares));
                listHash.put(listDataHeader.get(0), boughtStockList);
                listAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), selectedStock.name + " Added", Toast.LENGTH_SHORT).show();
                //preferences.edit().putString("profile", profileName.getText().toString()).apply();
                // Create new fragment and transaction
                //Fragment addStockFragment = new AddStockFragment();
                //FragmentManager transaction = getActivity().getSupportFragmentManager();
                //transaction.beginTransaction().replace(R.id.fragment, addStockFragment).commit();
            } catch (SQLiteException sqlException) {
                Toast.makeText(getActivity(), "Unable to add stock purchase.", Toast.LENGTH_SHORT).show();
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
                selectedStock = null;
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
                selectedStock = (StockInfo) parent.getAdapter().getItem(position);
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
                    stocks.add(new StockInfo(jsonPart.getString("n"), code, jsonPart.getString("e")));
                }

                ArrayAdapter<StockInfo> adapter = new ArrayAdapter<StockInfo>(getActivity(), android.R.layout.simple_list_item_1, stocks);
                stockAutoComplete.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static class StockInfo {
        private String name;
        private String code;
        private String exchange;

        public StockInfo(String name, String code, String exchange) {
            this.name = name;
            this.code = code;
            this.exchange = exchange;
        }

        @Override
        public String toString() {
            return code + " - " + name;
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
