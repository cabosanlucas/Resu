package com.projects.kabir.resu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class TodaysActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    DatabaseAccess mDatabaseAccess;
    User mFitnessUser;
    FitnessDataAccess mFitnessDataAccess;
    ListView listView;
    ArrayList<User> rankedUsers;
    FitAdapter fitAdapter;
    SwipeRefreshLayout refreshLayout;

    /**
     * create the main view screen as well as launch tasks to handle data updates etc
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up main content and toolbar items
        setContentView(R.layout.activity_todays);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up menu and menu navigation
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //create databaseAccess point
        mDatabaseAccess = DatabaseAccess.getInstance();
        for (User u:mDatabaseAccess.getUsers()) {
            Log.d("USER", u.toString());
        }
        //pull firebase username from intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(GoogleSignInActivity.MESSAGE_KEY);

        //if we have a user to work with update their data locally and on the database
        if (username != null) {
            Log.d("USERNAME", username);
            //get user object from that access point with the name passed into the activity
            mFitnessUser = mDatabaseAccess.getUser(username);
            mFitnessDataAccess = new FitnessDataAccess(this, mFitnessUser);
            mFitnessDataAccess.updateUserData();
            mDatabaseAccess.pushUser(mFitnessUser);
        }

        //build model for fitness adapter
        rankedUsers = mDatabaseAccess.getRankedUsers();
        fitAdapter = new FitAdapter(this, rankedUsers, FitAdapter.STEPS_ACTVITY);

        //display data from model
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(fitAdapter);

        //Set up refeshing using pull up
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_scrolling);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("USER DATA", mFitnessUser.toString());
                //update user data
                mFitnessDataAccess.updateUserData();
                //update ranking model based off new data
                rankedUsers = mDatabaseAccess.getRankedUsers();
                for (User s: rankedUsers) {
                    Log.d("User", s.toString());
                }
                //rebuild ui
                fitAdapter.notifyDataSetChanged();
            }
        });

    }


    /**
     * go back (primarily to handle back button if menu is open)
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * inflate the menu screen
     * @param menu menu frame to inflate
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button
     * @param item menu the menu item that called this task
     * @return is the option item selected or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
        Handle navigation view item clicks. Currently includes sign in / out
        and rank by steps / bicycle revlutions
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //
        int id = item.getItemId();

        if (id == R.id.sort_by_steps) {
            fitAdapter = new FitAdapter(this, rankedUsers, FitAdapter.STEPS_ACTVITY);
            listView.setAdapter(fitAdapter);
        } else if (id == R.id.nav_sign_out) {
            signOut(item);
        } else if (id == R.id.sort_by_revs) {
            fitAdapter = new FitAdapter(this, rankedUsers, FitAdapter.REV_ACTVITY);
            listView.setAdapter(fitAdapter);
        }

        //update ui to reflect changes on screen
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Handle a sign out callback
     * @param item item from which this was called
     */
    public void signOut(MenuItem item) {
        Intent signOutIntent = new Intent(this, GoogleSignInActivity.class);
        startActivityForResult(signOutIntent, GoogleSignInActivity.SIGN_OUT_REQUEST_CODE);
        startActivity(signOutIntent);
    }


}
