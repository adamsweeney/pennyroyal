package com.jibstream.pennyroyalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;
import java.util.Set;


public class CreateProfileFragment extends Fragment implements View.OnClickListener {
    SharedPreferences preferences;
    EditText profileName;

    public CreateProfileFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_profile, container, false);
        Button createProfileButton = (Button) v.findViewById(R.id.createProfileButton);
        profileName = (EditText)v.findViewById(R.id.profileEditText);
        createProfileButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createProfileButton:
                createProfile(view);
        }
    }

    public void createProfile(View view) {
        if (profileName.getText().toString().matches("")) {
            Toast.makeText(getActivity(), "Please Enter a Profile Name", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Pennyroyal", getActivity().MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS profiles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , name VARCHAR NOT NULL UNIQUE)");
            try {
                db.execSQL("INSERT INTO profiles (name) VALUES ('" + profileName.getText().toString() + "')");
                Toast.makeText(getActivity(), "Profile Created!", Toast.LENGTH_SHORT).show();
                preferences.edit().putString("profile", profileName.getText().toString()).apply();
                // Create new fragment and transaction
                Fragment addStockFragment = new AddStockFragment();
                FragmentManager transaction = getActivity().getSupportFragmentManager();
                transaction.beginTransaction().replace(R.id.fragment, addStockFragment).commit();
            } catch (SQLiteConstraintException sqlException) {
                Toast.makeText(getActivity(), "This profile name already exists.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unknown error occurred.", Toast.LENGTH_SHORT).show();
                Log.i("Error", e.getMessage());
            }

        }
    }
}
