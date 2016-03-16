package com.qman.revolver.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.qman.revolver.R;
import com.qman.revolver.utils.HttpManager;
import com.qman.revolver.utils.RequestPackage;

public class LoginActivity extends AppCompatActivity {

    private TextView info;
    private Button chatButton;
    private String userId;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        info = (TextView) findViewById(R.id.info);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        chatButton = (Button) findViewById(R.id.chatNowButton);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String content = "User ID: "
                        + loginResult.getAccessToken().getUserId()
                        + "\n" +
                        "Auth Token: "
                        + loginResult.getAccessToken().getToken();

                userId = loginResult.getAccessToken().getUserId();

                info.setText(content);
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt canceled.");
            }
        });

        if(AccessToken.getCurrentAccessToken() != null){
            chatButton.setVisibility(View.VISIBLE);
        } else {
            chatButton.setVisibility(View.GONE);
        }

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    chatButton.setVisibility(View.GONE);
                } else {
                    chatButton.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    public void startChat(View v){
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setUri("http://192.168.0.108:3333/");
        requestPackage.setMethod("POST");
        requestPackage.setParam("uid", userId);
        new NetworkTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,requestPackage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private class NetworkTask extends AsyncTask<RequestPackage,Void,String>{

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s != null){
                info.setText(s);
            }
        }
    }

}
