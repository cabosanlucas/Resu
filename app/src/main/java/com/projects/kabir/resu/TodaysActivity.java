package com.projects.kabir.resu;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.annotations.Since;

import java.util.ArrayList;
import java.util.Comparator;

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

        //UI
        setContentView(R.layout.activity_todays);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_scrolling);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("USER DATA", mFitnessUser.toString());
                mFitnessDataAccess.updateUserData();

                rankedUsers = mDatabaseAccess.getRankedUsers();
                for (User s: rankedUsers) {
                    Log.d("User", s.toString());
                }
                //fitAdapter = new FitAdapter(thisActivity, rankedUsers);
                fitAdapter.notifyDataSetChanged();
                //listView.setAdapter(fitAdapter);
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //on create in new actvity
        //create databaseAccess point
        mDatabaseAccess = DatabaseAccess.getInstance();
        for (User u:mDatabaseAccess.getUsers()) {
            Log.d("USER", u.toString());
        }
        //pull firebase username from intent
        Intent intent = getIntent();
        String username = intent.getStringExtra(GoogleSignInActivity.MESSAGE_KEY);

        if (username != null) {
            Log.d("USERNAME", username);
            //get user object from that access point with the name passed into the activity
            //this is your problem
            //you create your user before you can check if he exists
            mFitnessUser = mDatabaseAccess.getUser(username);
            mFitnessDataAccess = new FitnessDataAccess(this, mFitnessUser);

            //display data from model
            listView = (ListView) findViewById(R.id.list_view);

            rankedUsers = mDatabaseAccess.getRankedUsers();

            //FitAdapter fitAdapter = new FitAdapter(this, rankedUsers);

            fitAdapter = new FitAdapter(this, rankedUsers);
            listView.setAdapter(fitAdapter);

        }

//        mDatabaseAccess.pushUser(mFitnessUser);

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
     *
     * @param item menu the menu item that called this task
     * @return is the option item selected or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_sign_out) {
            signOut(item);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * onButtonClick for sign out page
     * @param item item from which this was called
     */
    public void signOut(MenuItem item) {
        Intent signOutIntent = new Intent(this, GoogleSignInActivity.class);
        startActivityForResult(signOutIntent, GoogleSignInActivity.RC_SIGN_OUT);
        startActivity(signOutIntent);
    }


}
