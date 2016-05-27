package com.jamjar.automator;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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

    public static void getPlaylists(){
        // Assumes userID has been acquired
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, userIDURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Spinner spinner = MainActivity.getPlaylistSpinner();
                        JSONArray respArr = response.optJSONArray("items");

                        for (int i = 0; i < respArr.length(); i++) {
                            try {
                                String plName = respArr.getJSONObject(i).optString("name");
                                MainActivity.getPlaylistAdapter().add(plName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SpotifyPlayer", "JSON request for userID failed");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params =  super.getHeaders();
                if (params == null)
                    params = new HashMap<>();
                params.put("Authorization", MainActivity.getSpotifyAuthTok());
                return params;
            }
        };

        AutomatorApplication.getRequestQueue().add(jsObjRequest);
    }

}