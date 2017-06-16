package com.jibstream.pennyroyalapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        preferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        checkForBasicSetup();
    }

    private void checkForBasicSetup() {
        String profile = preferences.getString("profile", "");
        boolean addedStock = preferences.getBoolean("addedStock", false);
        if (profile == "") {
            Fragment createProfileFragment = new CreateProfileFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, createProfileFragment).commit();
        } else if (!addedStock) {
            Fragment addStockFragment = new AddStockFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, addStockFragment).commit();
        } else {
            Fragment dashboardFragment = new DashboardFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, dashboardFragment).commit();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.add_stock) {
            AddStockFragment addStockFragment = new AddStockFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, addStockFragment).commit();
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            DashboardFragment dashboardFragment = new DashboardFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, dashboardFragment).commit();
            // Handle the camera action
        } else if (id == R.id.nav_portfolio) {
            /*CreateProfileFragment createProfile = new CreateProfileFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, createProfile).commit();*/
            PortfolioFragment portfolioFragment = new PortfolioFragment();
            FragmentManager transaction = getSupportFragmentManager();
            transaction.beginTransaction().replace(R.id.fragment, portfolioFragment).commit();
        } else if (id == R.id.nav_wish_list) {

        } else if (id == R.id.nav_alerts) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_switch) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
