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

    public FitAdapter(Context context, ArrayList<User> users) {
        super(context,R.layout.list_item, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

        TextView username = (TextView) convertView.findViewById(R.id.username);
        username.setText(user.name);

        TextView stepCount = (TextView) convertView.findViewById((R.id.step_count));
        stepCount.setText(Integer.toString(user.stepCount));

        return convertView;
    }

}
