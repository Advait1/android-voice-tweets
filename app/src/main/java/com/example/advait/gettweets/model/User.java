package com.example.advait.gettweets.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("screen_name")
    private String screenName;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getScreenName() { return "@"+screenName; }

    public String getName() {
        return name;
    }
}
