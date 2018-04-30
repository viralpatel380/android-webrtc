package me.pntutorial.pnrtcblog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import me.pntutorial.pnrtcblog.util.Constants;

/**
 * TODO: Uncomment mPubNub instance variable
 */
public class MainActivity extends Activity {
    private SharedPreferences mSharedPreferences;
    private TextView mUsernameTV;
    private EditText mCallNumET;
  private Pubnub mPubNub;
    public void initPubNub() {
        String stdbyChannel = this.username + Constants.STDBY_SUFFIX;
        this.mPubNub = new Pubnub(Constants.PUB_KEY, Constants.SUB_KEY);
        this.mPubNub.setUUID(this.username);
        try {
            this.mPubNub.subscribe(stdbyChannel, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.d("MA-success", "MESSAGE: " + message.toString());
                    if (!(message instanceof JSONObject)) return; // Ignore if not JSONObject
                    JSONObject jsonMsg = (JSONObject) message;
                    try {
                        if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;
                        String user = jsonMsg.getString(Constants.JSON_CALL_USER);
                        // Consider Accept/Reject call here
                        Intent intent = new Intent(MainActivity.this, VideoChatActivity.class);
                        intent.putExtra(Constants.USER_NAME, username);
                        intent.putExtra(Constants.JSON_CALL_USER, user);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }
    private String username;

    /**
     * TODO: "Login" by subscribing to PubNub channel + Constants.SUFFIX
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        // Return to Log In screen if no user is logged in.
        if (!this.mSharedPreferences.contains(Constants.USER_NAME)){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        this.username = this.mSharedPreferences.getString(Constants.USER_NAME, "");

        this.mCallNumET  = (EditText) findViewById(R.id.call_num);
        this.mUsernameTV = (TextView) findViewById(R.id.main_username);

        this.mUsernameTV.setText(this.username);  // Set the username to the username text view

        //TODO: Create and instance of Pubnub and subscribe to standby channel
        // In pubnub subscribe callback, send user to your VideoActivity
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Log out, remove username from SharedPreferences, unsubscribe from PubNub, and send user back
     *   to the LoginActivity
     */
    public void signOut(){
        // TODO: Unsubscribe from all channels with PubNub object ( pn.unsubscribeAll() )
        SharedPreferences.Editor edit = this.mSharedPreferences.edit();
        edit.remove(Constants.USER_NAME);
        edit.apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("oldUsername", this.username);
        startActivity(intent);
    }
}
