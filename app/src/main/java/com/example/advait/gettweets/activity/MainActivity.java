package com.example.advait.gettweets.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import com.example.advait.gettweets.R;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.advait.gettweets.adapter.TweetsAdapter;
import com.example.advait.gettweets.model.Tweet;
import com.example.advait.gettweets.model.TweetsResponse;
import com.example.advait.gettweets.rest.OAuthToken;
import com.example.advait.gettweets.rest.TwitterApi;
import com.example.advait.gettweets.util.NetworkUtil;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private String credentials = Credentials.basic("TWITTER_API_KEY");
    private final int REQ_CODE_SPEECH_OUTPUT = 143;

    TwitterApi twitterApi;
    OAuthToken token;
    RecyclerView recyclerView;
    TextView speechToText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.tweets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        speechToText = (TextView) findViewById(R.id.stt);

        NetworkUtil androidNetworkUtility = new NetworkUtil();
        if (!androidNetworkUtility.isConnected(this)) {
            Toast.makeText(getApplicationContext(), "Network connection not detected!", Toast.LENGTH_LONG).show();
        }

        Sensey.getInstance().init(getApplicationContext());

        TouchTypeDetector.TouchTypListener touchTypListener=new TouchTypeDetector.TouchTypListener() {
            @Override public void onTwoFingerSingleTap() {}
            @Override public void onThreeFingerSingleTap() {}
            @Override public void onDoubleTap() {}
            @Override public void onScroll(int scrollDirection) {}
            @Override public void onSingleTap() {}
            @Override public void onSwipe(int swipeDirection) {
                switch (swipeDirection) {
                    case TouchTypeDetector.SWIPE_DIR_LEFT:
                        openMic();
                        break;
                    default:
                        break;
                }
            }
            @Override public void onLongPress() {}
        };
        Sensey.getInstance().startTouchTypeDetection(getApplicationContext(), touchTypListener);

        createTwitterApi();
        twitterApi.postCredentials("client_credentials").enqueue(tokenCallback);

        openMic();
    }

    /**
     * Set up retrofit instance with twitter api credentials
     */
    private void createTwitterApi() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                        token != null ? token.getAuthorization() : credentials);

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterApi.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        twitterApi = retrofit.create(TwitterApi.class);
    }

    /**
     * Request oauth token callback
     */
    Callback<OAuthToken> tokenCallback = new Callback<OAuthToken>() {
        @Override
        public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
            if (response.isSuccessful()) {
                token = response.body();
                Log.d("tweetsCallback", token.getAuthorization());
                createTwitterApi();
            } else {
                Toast.makeText(MainActivity.this, "Failure while requesting token", Toast.LENGTH_LONG).show();
                Log.d("RequestTokenCallback", "Code: " + response.code() + "Message: " + response.message());
            }
        }
        @Override
        public void onFailure(Call<OAuthToken> call, Throwable t) {
            t.printStackTrace();
        }
    };


    /**
     * Start listening for the user's voice
     */
    private void openMic() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi Speak Now");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        }
        catch (ActivityNotFoundException tim) {
            Toast.makeText(getApplicationContext(), "Couldn't start Recognizer Activity!", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Process result for speech recognizer
     * @param requestCode request code
     * @param resultCode result code
     * @param data data from recognizer
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_OUTPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> voiceText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String searchQuery = "Fetched popular tweets for: " + voiceText.get(0);
                    speechToText.setText(searchQuery);
                    getTweets("%40" + voiceText.get(0));
                }
                break;
            }
        }
    }

    /**
     * Fetch tweets matching search query
     * @param searchQuery search query
     */
    private void getTweets(String searchQuery) {
        if (token == null) {
            twitterApi.postCredentials("client_credentials").enqueue(tokenCallback);
            createTwitterApi();
        } else {
            if (searchQuery.contains("#")) {
                twitterApi.getTweets(searchQuery, "popular").enqueue(tweetsCallback);
            } else {
                twitterApi.getTweets(searchQuery, "recent").enqueue(tweetsCallback);
            }

        }
    }

    /**
     * Callback for fetch tweets
     */
    Callback<TweetsResponse> tweetsCallback = new Callback<TweetsResponse>() {
        @Override
        public void onResponse(Call<TweetsResponse> call, Response<TweetsResponse> response) {
            if (response.isSuccessful()) {
                List<Tweet> tweets = response.body().getResults();
                recyclerView.setAdapter(new TweetsAdapter(tweets, R.layout.list_item_tweet, getApplicationContext()));
            } else {
                Toast.makeText(MainActivity.this, "Failure while requesting tweets", Toast.LENGTH_LONG).show();
                Log.d("tweetsCallback", "Code: " + response.code() + " Message: " + response.message());
            }
        }
        @Override
        public void onFailure(Call<TweetsResponse> call, Throwable t) {
            t.printStackTrace();
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Setup onTouchEvent for detecting type of touch gesture
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }
}
