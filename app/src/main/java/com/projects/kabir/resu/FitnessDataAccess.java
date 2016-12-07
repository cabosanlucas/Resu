package com.projects.kabir.resu;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

/**
 * Created by kabir on 11/30/16.
 */

public class FitnessDataAccess {

    public GoogleApiClient mFitnessClient;
    public static final String TAG = "Resu_Log";

    public TodaysActivity mActivity;
    private User fitnessUser;
    private DatabaseAccess databaseAccess;
    public UpdateUserData updateTask;



    public FitnessDataAccess(TodaysActivity activity, User user) {
        mActivity = activity;
        fitnessUser = user;
        mFitnessClient = buildFitnessClient();
        databaseAccess = DatabaseAccess.getInstance();
        updateTask = new UpdateUserData();
    }


    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. Authentication  occasionally fails for known reasons,
     *  in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address.
     */
    private GoogleApiClient buildFitnessClient() {

        // Create the Google API Client
        final GoogleApiClient client = new GoogleApiClient.Builder(mActivity)
                //Add access to history and reporting
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                new UpdateUserData().execute();
                                subscribe();
                            }
                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .enableAutoManage(mActivity, 1, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString() + "Error message: " + result.getErrorMessage());
                        Snackbar.make(
                                mActivity.findViewById(R.id.activity_today),
                                "Exception while connecting to Google Play services: " +
                                        result.getErrorMessage(),
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();

        return client;
    }

    //update the user object associated with the currently authenticated user
    public void updateUserData() {
        new UpdateUserData().execute();

    }

    /**
     *  Create and execute a DataReadRequest access data sent from the firebase databse using a
     *  AsyncTask, so that User Experince is not impacted. Q
     *  Update the UI after data pull is successful.
     */

    public class UpdateUserData extends AsyncTask<Void, Void, User> {

        protected User doInBackground(Void... params) {
            Log.i(TAG, "In Background");

            // Begin by creating the query.
            DataReadRequest readRequest = queryFitnessData();

            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mFitnessClient, readRequest).await(1, TimeUnit.MINUTES);

            //access relevant aggregate data
            int totalSteps = getStepCount(dataReadResult);
            double totalWatts = getTotalPowerUsage(dataReadResult);

            //Update the user object
            fitnessUser.updateStepCount(totalSteps);
            fitnessUser.updateWattsUsed(totalWatts);

            databaseAccess.pushUser(fitnessUser);
            return fitnessUser;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            mActivity.refreshLayout.setRefreshing(false);
        }
    }


    /**
     * Return a DataReadRequest for all step count changes in the past month.
     */
    public DataReadRequest queryFitnessData() {
        // Set a start and end time for our data, using a start time of 1 month before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // get both stepcount and cycling data
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
                    //store it in daily totals
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();

            return readRequest;
    }

    /**
     * Sum total steps over the peroid of the data request
     * @param dataReadResult the data over the given period
     * @return the total step count of the given data
     */
    public int getStepCount(DataReadResult dataReadResult) {
        // Since the DataReadRequest object specified aggregated data, data will be returned
        // as buckets containing DataSets.
        int steps = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned step buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            //Sum the steps
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    for(DataPoint dp: dataSet.getDataPoints()) {
                        steps += dp.getValue(Field.FIELD_STEPS).asInt();
                    }
                }
            }
        }
        return steps;
    }

    private double getTotalPowerUsage(DataReadResult dataReadResult) {
        double watts = 0;
        DataSet dataSet = dataReadResult.getDataSet(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE);
        for(DataPoint dp: dataSet.getDataPoints()) {
            watts += dp.getValue(Field.FIELD_REVOLUTIONS).asInt();
        }
        Log.d(TAG, "" + watts);
        return watts;

    }

    /**
     * Subscribe to the relevant DataTypes for this application
     */
    public void subscribe() {
        //Record step counts
        Fitness.RecordingApi.subscribe(mFitnessClient, DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    //For debug
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });

        //Record pedals
        Fitness.RecordingApi.subscribe(mFitnessClient, DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });
    }
}

