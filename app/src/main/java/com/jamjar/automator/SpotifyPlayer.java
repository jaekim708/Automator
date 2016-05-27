package com.jamjar.automator;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by jae on 5/27/16.
 */
public class SpotifyPlayer {
    private static String userID = "";
    private static final String userIDURL = "https://api.spotify.com/v1/me";
    private static String playlistsURL = "";

    public static void setup() {
        if (userID.equals("")) {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, userIDURL, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            userID = response.optString("id");
                            playlistsURL = "https://api.spotify.com/v1/users/" +
                                    userID + "/playlists";

                            System.out.println("USERID IS " + userID + " playlistsURL is " + playlistsURL);
                            getPlaylists();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("SpotifyPlayer", "JSON request for userID failed");
                            Log.d("SpotifyPlayer", error.toString());
                        }

                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + MainActivity.getSpotifyAuthTok());
                    return params;
                }
            };
            AutomatorApplication.getRequestQueue().add(jsObjRequest);
        }
    }

    public static void getPlaylists(){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, playlistsURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
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
                        Log.d("SpotifyPlayer", "JSON request for playlists failed");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + MainActivity.getSpotifyAuthTok());
                return params;
            }
        };

        AutomatorApplication.getRequestQueue().add(jsObjRequest);
    }

}