package com.example.keepmynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = "LoginRegisterActivity";
    int AUTHUI_REQUEST_CODE= 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        if(FirebaseAuth.getInstance().getCurrentUser() !=null){
            Intent intent=new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    public void handleLoginRegister(View view) {

        List<AuthUI.IdpConfig> provider = ImmutableList.of(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provider)
                .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                .setLogo(R.drawable.notesapplogo)
                .setAlwaysShowSignInMethodScreen(true)
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, AUTHUI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AUTHUI_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                //we have signed in the user or we have new user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                Log.d(TAG, "onActivityResult: " +user.getEmail());

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                //signing in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response==null){
                    Log.d(TAG, "onActivityResult: The user has cancelled the sign in request ");
                }else{
                    Log.e(TAG, "onActivityResult: ", response.getError());
                }
            }
        }
    }
}