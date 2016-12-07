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
 * Implements the singleton design pattern to provide unified singular access
 */
public class DatabaseAccess {
    private static DatabaseAccess databaseAccess;
    public FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mLeaderboardRef;
    private DatabaseReference mUsersRef;
    private ArrayList<String> mLeaderboardMembersList;
    private ArrayList<User> mUserList;


    public static final String LEADERBOARD_KEY = "leaderboards";
    public static final String USERS_KEY = "users";

    public static DatabaseAccess getInstance() {
        return databaseAccess;
    }

    static {
        databaseAccess = new DatabaseAccess();
    }
    private DatabaseAccess() {
        mUsersRef = firebaseDatabase.getReference(USERS_KEY);
        mLeaderboardRef = firebaseDatabase.getReference(LEADERBOARD_KEY);

        mLeaderboardMembersList = new ArrayList<>(0);
        mUserList = new ArrayList<>(0);

        addLeaderUpdateListener();

        addUserUpdateListener();

    }


    public ArrayList<String> getUsernames() {
        return mLeaderboardMembersList;
    }

    public ArrayList<User> getUsers() {
        return mUserList;
    }

    public ArrayList<User> getRankedUsers() {
        ArrayList<User> toSort = getUsers();
        Collections.sort(toSort, new Comparator<User>() {
            @Override
            public int compare(User t1, User t2) {
                return t2.stepCount - t1.stepCount;
            }
        });
        return toSort;
    }

    public void addLeaderUpdateListener() {
        mLeaderboardRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String user = dataSnapshot.getValue(String.class);
                mLeaderboardMembersList.add(user);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String updatedUserName = dataSnapshot.getValue(String.class);
                mLeaderboardMembersList.remove(s);
                mLeaderboardMembersList.add(updatedUserName);
            }

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

    public void addUserUpdateListener() {
        mUsersRef.addValueEventListener(new ValueEventListener() {

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

    public User getUser(String username) {
        for (User u: mUserList){
            if (u.name.equals(username)) {
                return u;
            }
        }
        return null;
    }

    public void pushUser(User user) {
        if (containsUser(user)) {
            updateStepCount(user);
        } else {
            mUsersRef.child(user.name).setValue(user);
        }
    }

    public void addUserToBoard(String username) {
        mLeaderboardRef.child(username).setValue(username);
    }

    public boolean containsUser(User user) {
        return (mUserList.contains(user) || mLeaderboardMembersList.contains(user.name));
    }

    private void updateStepCount(User user) {
        mUsersRef.child(user.name).setValue(user);
    }

}
