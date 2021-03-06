package com.example.charityapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * @description Class gets all data needed for donor recycle viewer when showing all donors
 *
 * @author Jack Bates and AJ Thut
 * @date_created 04/12/20
 * @date_modified 05/01/20
 *
 */
public class VolunteerHours extends AppCompatActivity {
    //viewer, database references
    RecyclerView recyclerView;
    DatabaseReference ref;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volunteer_hours);
        database = FirebaseDatabase.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("VolHours");
        //create the viewer
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();
        //gets all data needed to create/fill the viewer
        FirebaseRecyclerAdapter<Volunteer, VolunteerAdapter> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Volunteer, VolunteerAdapter>(Volunteer.class, R.layout.volrow, VolunteerAdapter.class, ref){
                    protected void populateViewHolder(VolunteerAdapter holder, Volunteer v, int i){
                        holder.setView(getApplicationContext(), v.getName(), v.getHours());
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
