package com.jamjar.automator;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Spinner;

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
    private static final String userIDURL = "https://api.spotify.com/v1/me"; // CAN I DELETE?
    private static final String playlistsURL = "https://api.spotify.com/v1/me/playlists";
    private static HashMap<String, String> mPlaylistIDs = new HashMap<>();
    private static final IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    private static BroadcastReceiver mBr;

    public static void setup(final Context context, final Activity activity) {
        mBr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("state", 2) == 1) // if plugged in
                    startPlaylist(activity);
            }
        };

        if (userID.equals("")) {
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, userIDURL, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            userID = response.optString("id");

                            System.out.println("USERID IS " + userID + " playlistsURL is " + playlistsURL);
                            getPlaylists(context);

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
        else
            getPlaylists(context);
    }

    public static void startPlaylist(Activity activity){
        Spinner mPlaylistSpinner = (Spinner) activity.findViewById(R.id.playlistSpinner);
        String pl = mPlaylistSpinner.getSelectedItem().toString();
        System.out.println(pl + " " + mPlaylistIDs.get(pl));
        MainActivity.getPlayer().play("spotify:user:" + userID + ":playlist:" + mPlaylistIDs.get(pl));
        System.out.println("STARTING TO PLAY");
    }

    public static void getPlaylists(final Context context){
        mPlaylistIDs.clear();
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
                                mPlaylistIDs.put(plName, respArr.getJSONObject(i).optString("id"));
                                context.registerReceiver(mBr, receiverFilter);

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

    public static BroadcastReceiver getBroadcastReceiver(){
        return mBr;
    }
}