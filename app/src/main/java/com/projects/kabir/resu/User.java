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

    public User() {

    }
    public User(String name) {
        this.name = name;
        stepCount =0;
    }

    public User(String name, int stepCount) {
        this.name = name;
        this.stepCount = stepCount;
    }

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

        //if (stepCount != user.stepCount) return false;
        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + stepCount;
        return result;
    }


}
