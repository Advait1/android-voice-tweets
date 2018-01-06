package com.example.advait.gettweets.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TweetsResponse {

    @SerializedName("statuses")
    private List<Tweet> statuses;

    public List<Tweet> getResults() {
        return statuses;
    }
}
