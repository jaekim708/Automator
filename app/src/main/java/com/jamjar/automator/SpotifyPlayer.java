package com.jamjar.automator;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by jae on 5/27/16.
 */
public class SpotifyPlayer {
    private static String userID = "";
    private static final String userIDURL = "https://api.spotify.com/v1/me";;
    private static String playlistsURL = "";

    public static void setup() {
        if (userID.equals("")) {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, userIDURL, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            userID = response.optString("id");
                            playlistsURL = "https://api.spotify.com/v1/users/" +
                                    userID + "/playlists";

                            System.out.println("USERID IS " + userID);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("SpotifyPlayer", "JSON request for userID failed");
                        }

                    });
            AutomatorApplication.getRequestQueue().add(jsObjRequest);
        }
    }
/*
    public static ArrayList<String> getPlaylists(){
        // Assumes userID has been acquired
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userIDURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        userID = response.optString("id");
                        System.out.println("USERID IS " + userID);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SpotifyPlayer", "JSON request for userID failed");
                    }

                });

    return null;
    }
    */
}