package com.jamjar.automator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;


public class MainActivity extends AppCompatActivity implements PlayerNotificationCallback,
        ConnectionStateCallback {

    private static Boolean mOn = false;
    private static TextView mOnOff;
    private static int REQUEST_PERMISSION_READ_CALENDAR = 4;
    private static final String CLIENT_ID = "d301ecd6a9054daabab3b7d846540edc";
    private static final String REDIRECT_URI = "automator://callback/";
    private static Player mPlayer;
    private static final int SPOTIFY_REQUEST_CODE = 1337;

    private static String mSpotifyAuthTok;

    private SpotifyPlayer spotifyPlayer = new SpotifyPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mSToggle = (Button) findViewById(R.id.toggleSilencer);
        Button mPToggle = (Button) findViewById(R.id.togglePlayer);

        mOnOff = (TextView) findViewById(R.id.onOff);
        mOnOff.setText(R.string.off);
        CalAccess.setup(getApplicationContext());

        mSToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mOn) {
                    mOn = false;
                    mOnOff.setText(R.string.off);
                    CalAccess.cancelAlarms();
                } else {
                    mOn = true;
                    mOnOff.setText(R.string.on_no_events);
                    getCalendarPermissions();
                    CalAccess.update(getApplicationContext());
                }
            }
        });

        mPToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mOn) {
                    mOn = false;
                    mOnOff.setText(R.string.off);
                    CalAccess.cancelAlarms();
                } else {
                    mOn = true;
                    mOnOff.setText(R.string.on_no_events);
                    spotifyLogin();
                }
            }
        });
    }

    private void getCalendarPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    REQUEST_PERMISSION_READ_CALENDAR);
        }
    }

    public static TextView getText(){
        return mOnOff;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // if request code was from Spotify
        if (requestCode == SPOTIFY_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mSpotifyAuthTok = response.getAccessToken();
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    // Spotify implementation functions
    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    private void spotifyLogin(){
        // Begin Spotify login code - calls onActivityResult()
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private", "playlist-read-collaborative"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
        // End Spotify login code

    }

    public static String getSpotifyAuthTok(){
        return mSpotifyAuthTok;
    }

}
