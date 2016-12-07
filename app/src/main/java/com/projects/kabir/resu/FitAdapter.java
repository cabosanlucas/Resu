package com.projects.kabir.resu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kabir on 12/6/16.
 */

public class FitAdapter extends ArrayAdapter<User> {

    public static final int STEPS_ACTVITY = 0;
    public static final int REV_ACTVITY = 1;
    int mActvityType;

    /**
     * Build an adapter depending on the type of data to display
     * @param context acvity to display on
     * @param users Model to display
     * @param actvityType kind of actvity to display
     */
    public FitAdapter(Context context, ArrayList<User> users, int actvityType) {
        super(context,R.layout.list_item, users);
        mActvityType = actvityType;
    }

    /**
     * Display data
     * @param position the list item to create
     * @param convertView the list item to reuse
     * @param parent the list view to add to
     * @return the list_item to add to the scrolling list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

        TextView username = (TextView) convertView.findViewById(R.id.username);
        username.setText(user.name);

        //Depending on what kind of data we wish to show, modify the view
        TextView stepCount = (TextView) convertView.findViewById((R.id.step_count));
        if (mActvityType == STEPS_ACTVITY) {
            stepCount.setText(Integer.toString(user.stepCount));
        } 
        
        if (mActvityType == REV_ACTVITY) {
            stepCount.setText(Double.toString(user.revs));
        }

        return convertView;
    }

}
