package com.example.advait.gettweets.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import com.example.advait.gettweets.R;
import com.example.advait.gettweets.model.Tweet;
import com.example.advait.gettweets.model.User;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.TweetViewHolder> {

    private List<Tweet> tweets;
    private int rowLayout;
    private Context context;

    static class TweetViewHolder extends RecyclerView.ViewHolder {

        LinearLayout moviesLayout;
        TextView name;
        TextView screenName;
        TextView tweet;
        TextView date;

        TweetViewHolder(View v) {
            super(v);
            moviesLayout = (LinearLayout) v.findViewById(R.id.tweets_layout);
            name = (TextView) v.findViewById(R.id.name);
            screenName = (TextView) v.findViewById(R.id.screen_name);
            tweet = (TextView) v.findViewById(R.id.text);
            date = (TextView) v.findViewById(R.id.date);
        }
    }

    public TweetsAdapter(List<Tweet> movies, int rowLayout, Context context) {
        this.tweets = movies;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    @Override
    public TweetsAdapter.TweetViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TweetViewHolder(view);
    }


    @Override
    public void onBindViewHolder(TweetViewHolder holder, final int position) {

        User user = tweets.get(position).getUser();

        StringBuilder displayDate = new StringBuilder(tweets.get(position).getCreatedAt());
        displayDate.setLength(16);
        displayDate.delete(0, 4);

        holder.name.setText(user.getName());
        holder.screenName.setText(user.getScreenName());
        holder.date.setText(displayDate.toString());
        holder.tweet.setText(tweets.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}
