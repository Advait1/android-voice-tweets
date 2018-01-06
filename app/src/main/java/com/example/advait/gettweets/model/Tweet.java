package com.example.advait.gettweets.model;

import com.google.gson.annotations.SerializedName;

public class Tweet {

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("text")
    private String text;

    @SerializedName("in_reply_to_status_id")
    private String inReplyToStatusId;

    @SerializedName("in_reply_to_user_id")
    private String inReplyToUserId;

    @SerializedName("in_reply_to_screen_name")
    private String inReplyToScreenName;

    @SerializedName("user")
    private User twitterUser;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return twitterUser;
    }

    @Override
    public String toString(){
        return getText();
    }
}
