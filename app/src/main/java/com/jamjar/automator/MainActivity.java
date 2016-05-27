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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

// Need to Launch spotify, not just play, in order to be market-safe



public class MainActivity extends AppCompatActivity implements PlayerNotificationCallback,
        ConnectionStateCallback {

    private static Boolean mCalOn = false;
    private static TextView mCalText;
    private static int REQUEST_PERMISSION_READ_CALENDAR = 4;

    private static Boolean mSpotOn = false;
    private static Boolean loggedIn = false;
    private static final String CLIENT_ID = "d301ecd6a9054daabab3b7d846540edc";
    private static final String REDIRECT_URI = "automator://callback/";
    private static Player mPlayer;
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    private static String mSpotifyAuthTok;
    private static ArrayAdapter<CharSequence> mPlaylistAdapter;
    private static Spinner mPlaylistSpinner;

    private SpotifyPlayer spotifyPlayer = new SpotifyPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mSToggle = (Button) findViewById(R.id.toggleSilencer);
        final Button mPToggle = (Button) findViewById(R.id.togglePlayer);

        mPlaylistSpinner = (Spinner) findViewById(R.id.playlistSpinner);
        mPlaylistAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mPlaylistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlaylistSpinner.setAdapter(mPlaylistAdapter);

        mCalText = (TextView) findViewById(R.id.calText);
        mCalText.setText(R.string.calOff);
        CalAccess.setup(getApplicationContext());

        mSToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCalOn) {
                    mCalOn = false;
                    mCalText.setText(R.string.calOff);
                    CalAccess.cancelAlarms();
                } else {
                    mCalOn = true;
                    mCalText.setText(R.string.on_no_events);
                    getCalendarPermissions();
                    mCalText.setText(CalAccess.update(getApplicationContext()));
                }
            }
        });

        mPToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mSpotOn) {
                    mSpotOn = false;
                    mPToggle.setText(R.string.playerOff);
                    try {
                        unregisterReceiver(SpotifyPlayer.getBroadcastReceiver());
                        // clear spinner?
                    } catch (IllegalArgumentException e) {
                        // Receiver wasn't registered, so do nothing
                    }
                } else {
                    mSpotOn = true;
                    mPToggle.setText(R.string.playerOn);
                    if (!loggedIn)
                        spotifyLogin();
                    else
                        SpotifyPlayer.getPlaylists(getApplicationContext());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // if request code was from Spotify
        if (requestCode == SPOTIFY_REQUEST_CODE) {
            loggedIn = true;
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mSpotifyAuthTok = response.getAccessToken();
                Config playerConfig = new Config(this, mSpotifyAuthTok, CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        SpotifyPlayer.setup(getApplicationContext(), MainActivity.this);
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

    public static Spinner getPlaylistSpinner(){
        return mPlaylistSpinner;
    }

    public static ArrayAdapter<CharSequence> getPlaylistAdapter(){
        return mPlaylistAdapter;
    }

    public static Player getPlayer(){
        return mPlayer;
    }
}
