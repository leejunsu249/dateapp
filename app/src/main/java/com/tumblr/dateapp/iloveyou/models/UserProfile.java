package com.tumblr.dateapp.iloveyou.models;



import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserProfile implements Serializable {

    private String id;
    private String userName;
    public String pushToken;
    public String size;
    public int spinner_pos;
    @Exclude
    private Bitmap avatar;

    // required empty constructor for firebase loading
    public UserProfile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getGender() {
        return size;
    }

    public void setGender(String size) {
        this.size = size;
    }

    public int getSpinnerPos() { return spinner_pos; }

    public void setSpinner_pos(int spinner_pos) {
        this. spinner_pos =  spinner_pos;
    }

    @Exclude
    public Bitmap getAvatar() {
        return avatar;
    }

    @Exclude
    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }
}
