package com.projects.kabir.resu;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kabir on 12/1/16.
 */

/**
 * Implements the singleton design pattern to provide unified singular access to our
 * firebase database. Absatracts interation and can add, remove, modify, and orginizes database
 * information relvent for the application
 */
public class DatabaseAccess {
    private static DatabaseAccess databaseAccess;
    public FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mLeaderboardRef;
    private DatabaseReference mUsersRef;

    //User objects and rankings updated based off the database and abstracted for the rest of the
    //application
    private ArrayList<String> mLeaderboardMembersList;
    private ArrayList<User> mUserList;

    public static final String LEADERBOARD_KEY = "leaderboards";
    public static final String USERS_KEY = "users";

    //Create this singleton object
    static {
        databaseAccess = new DatabaseAccess();
    }

    public static DatabaseAccess getInstance() {
        return databaseAccess;
    }

    private DatabaseAccess() {
        mUsersRef = firebaseDatabase.getReference(USERS_KEY);
        mLeaderboardRef = firebaseDatabase.getReference(LEADERBOARD_KEY);

        mLeaderboardMembersList = new ArrayList<>(0);
        mUserList = new ArrayList<>(0);

        addLeaderUpdateListener();
        addUserUpdateListener();

    }

    /**
     * Get the names of all users of this application
     * @return a list of usernames
     */
    public ArrayList<String> getUsernames() {
        return mLeaderboardMembersList;
    }

    /**
     * Get all users signed up for this application and their data
     * @return a list of user objects
     */
    public ArrayList<User> getUsers() {
        return mUserList;
    }

    /**
     * Get a ranking of each of the users for this leaderboard
     * @return a ranked list of user objects
     */
    public ArrayList<User> getRankedUsers() {
        ArrayList<User> toSort = getUsers();
        //Sort collection using comparator
        Collections.sort(toSort, new Comparator<User>() {
            @Override
            public int compare(User t1, User t2) {
                return t2.stepCount - t1.stepCount;
            }
        });
        return toSort;
    }

    /**
     * Add a listner for updates to the leaderboards users
     */
    public void addLeaderUpdateListener() {
        mLeaderboardRef.addChildEventListener(new ChildEventListener() {

            /**
             * if a new username is added to a board add that user to the locally maintanted
             * list of username
             * @param dataSnapshot a json representation of this user
             * @param s the name of the pervious object at this location
             */
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String user = dataSnapshot.getValue(String.class);
                mLeaderboardMembersList.add(user);
            }

            /**
             * If a username is updated, remove the old username from our model and add the updated one
             * @param dataSnapshot json representation of this user
             * @param s the name of the pervious object at this location
             */
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String updatedUserName = dataSnapshot.getValue(String.class);
                mLeaderboardMembersList.remove(s);
                mLeaderboardMembersList.add(updatedUserName);
            }

            /**
             * If a user is deleted, remove him from our model
             * @param dataSnapshot a json representation of this user
             */
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String user = dataSnapshot.getValue(String.class);
                if (mLeaderboardMembersList.contains(user)) {
                    mLeaderboardMembersList.remove(user);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase error: ", databaseError.getMessage());
            }

        });
    }

    /**
     * Add a listner for updates to the users of this application
     */
    public void addUserUpdateListener() {
        mUsersRef.addValueEventListener(new ValueEventListener() {

            /**
             * If there is a change to the users rebuild the local userlist
             * @param dataSnapshot a json repsention of all users
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserList = new ArrayList<User>(mUserList.size());
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    User fitnessUser = userSnapshot.getValue(User.class);
                    mUserList.add(fitnessUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase error: ", databaseError.getMessage());
            }
        });
    }

    /**
     * Get a user object based off a username
     * @param username the name of the user we are truing to find
     * @return the user object corresponding to the name
     */
    public User getUser(String username) {
        for (User u: mUserList){
            if (u.name.equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Add a user object to the database
     * @param user the user to push
     */
    public void pushUser(User user) {
        if (containsUser(user)) {
            mUsersRef.child(user.name).setValue(user);
        } else {
            mUsersRef.child(user.name).setValue(user);
        }
    }

    /**
     * Add a username to the list of usernames
     * @param username
     */
    public void addUserToBoard(String username) {
        mLeaderboardRef.child(username).setValue(username);
    }

    /**
     * If this user exists anywhere in our database (either by name or by object) tell us
     * @param user user we are looking for
     * @return if this user is in our database
     */
    public boolean containsUser(User user) {
        return (mUserList.contains(user) || mLeaderboardMembersList.contains(user.name));
    }

}
