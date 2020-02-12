package com.example.charityapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText userNameTxt;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        userNameTxt = findViewById(R.id.userName_txt);

        loadInfo();

        findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameTxt.getText().toString();
                if(userName.isEmpty()){
                    userNameTxt.setError("Must enter user name");
                    userNameTxt.requestFocus();
                    return;
                }

                FirebaseUser user = mAuth.getCurrentUser();

                if(user != null){
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build();

                    user.updateProfile(profile)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), "User infromation Updated", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                                }
                            });
                }
            }
        });
    }

    protected void onStart(){
        super.onStart();

        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void loadInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        String userName = user.getDisplayName();

        if(user != null){
            if(user.getDisplayName() != null){
                userNameTxt.setText(userName);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ActionLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));

        }
        return true;
    }
}
