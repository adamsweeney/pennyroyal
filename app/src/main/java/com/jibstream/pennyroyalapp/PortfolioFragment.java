package com.jibstream.pennyroyalapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {

    HashMap<String, Float> boughtStockCurrentPrices;
    SharedPreferences preferences;
    ArrayList<String> boughtStockList;
    ArrayAdapter<String> portfolioAdapter;

    public PortfolioFragment() {
        boughtStockCurrentPrices = new HashMap<>();
        boughtStockList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        preferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        ListView portfolioList = (ListView) view.findViewById(R.id.boughtStockListView);
        portfolioAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, boughtStockList);
        portfolioList.setAdapter(portfolioAdapter);
        fetchCurrentPricesForBoughtStock();
        return view;
    }

    private void fetchCurrentPricesForBoughtStock() {
        int profileId = 0;
        SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
        Cursor cursor =  db.rawQuery("SELECT id FROM profiles WHERE name = '" + preferences.getString("profile", "") + "' LIMIT 1", null);
        int profileIdIndex = cursor.getColumnIndex("id");
        if(cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                profileId = cursor.getInt(profileIdIndex);
            } while (cursor.moveToNext());
        }
        cursor =  db.rawQuery("SELECT exchange, code, name FROM buys WHERE profile = " + profileId + " GROUP BY code", null);
        int codeIndex = cursor.getColumnIndex("code");
        int exchangeIndex = cursor.getColumnIndex("exchange");
        int nameIndex = cursor.getColumnIndex("name");
        if(cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                String code = cursor.getString(codeIndex);
                String exchange = cursor.getString(exchangeIndex);
                String name = cursor.getString(nameIndex);
                boughtStockList.add(name);
                CurrentStockPriceTask stockPriceTask = new CurrentStockPriceTask();
                stockPriceTask.execute("http://www.alphavantage.co/query?function=GLOBAL_QUOTE&apikey=PKK5&symbol=" + exchange + ":" + code);
            } while (cursor.moveToNext());
        }
        portfolioAdapter.notifyDataSetChanged();
    }

    private class CurrentStockPriceTask extends AsyncTask<String, Void, String> {

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
            try {
                JSONObject json = new JSONObject(result);
                JSONObject realTimeObject = json.getJSONObject("Realtime Global Securities Quote");
                String code = realTimeObject.getString("01. Symbol").toUpperCase();
                String exchange = realTimeObject.getString("02. Exchange Name").toUpperCase();
                double price = realTimeObject.getDouble("03. Latest Price");
                boughtStockCurrentPrices.put(exchange + ":" + code, (float)price);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
