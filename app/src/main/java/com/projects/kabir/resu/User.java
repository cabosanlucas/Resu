package com.projects.kabir.resu;

import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;

import java.util.HashMap;

/**
 * Created by kabir on 12/3/16.
 */

public class User {
    public String name;
    public int stepCount;
    public double revs;

    /**
     * Empty constructer for firsebase builds
     */
    public User() {

    }

    /**
     * Build a user for whom we have no data
     * @param name name of user
     */
    public User(String name) {
        this.name = name;
        stepCount =0;
    }

    /**
     * Build a user who we have data for
     * @param name username
     * @param stepCount steps walked
     */
    public User(String name, int stepCount, double revs) {
        this.name = name;
        this.stepCount = stepCount;
        this.revs = revs;
    }

    /**
     * update step count
     * @param steps new step count
     */
    public void updateStepCount(int steps) {
        stepCount = steps;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", stepCount=" + stepCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + stepCount;
        return result;
    }

    public void updateWattsUsed(double totalWatts) {
        revs = totalWatts;
    }
}
