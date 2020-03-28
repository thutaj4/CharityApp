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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DonorActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ref;
    FirebaseDatabase database;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor);

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Events");

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Event, recycleAdapter> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, recycleAdapter>(Event.class, R.layout.row, recycleAdapter.class, ref){
                    protected void populateViewHolder(recycleAdapter holder, Event event, int i){
                        holder.setView(getApplicationContext(), event.getName(), event.getProgram(), event.getDate(), event.getTime());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(DonorActivity.this, DonorEventDetails.class);
                                intent.putExtra("Name", event.getName());
                                intent.putExtra("Program", event.getProgram());
                                intent.putExtra("Description", event.getDescription());
                                intent.putExtra("Date", event.getDate());
                                intent.putExtra("Time", event.getTime());
                                intent.putExtra("Funds", event.getFunding());
                                intent.putExtra("Volunteers", event.getVolunteers());
                                intent.putExtra("VolunteersNeeded", event.getVolunteersNeeded());

                                startActivity(intent);
                            }
                        });

                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String temp = user.getDisplayName().replaceAll("Donor:", "");
                                //check if the max amount of volunteers has been reached
                                if(event.getVolunteersNeeded()<= 0 && !event.getVolunteers().contains(temp)){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DonorActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle("Volunteer Limit Reached");
                                    builder.setMessage("The maximum amount of volunteers needed for the event has been reached!");

                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();
                                }
                                //check if that users name is already signed up
                                else if(event.getVolunteers().contains(temp)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DonorActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle("Un-Volunteer");
                                    builder.setMessage("Would you like to un sign up for this event?");

                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String eventname = event.getName();
                                            Query eventquery = ref.orderByChild("name").equalTo(eventname);
                                            eventquery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot eventshot: dataSnapshot.getChildren()){
                                                        String original = event.getVolunteers();
                                                        String newVolList;
                                                        if (event.getVolunteers().length() <= temp.length() + 1) {
                                                            newVolList = original.replace(temp, "");
                                                        } else if(event.getVolunteers().substring(0, temp.length()).equals(temp)) {
                                                            newVolList = original.replace(temp + ",", "");
                                                        } else{
                                                            newVolList = original.replace("," + temp, "");
                                                        }

                                                        ref.child(eventshot.getKey()).child("volunteers").setValue(newVolList);
                                                        //get the number of volunteers and add one
                                                        int val = event.getNumVolunteers() - 1;
                                                        ref.child(eventshot.getKey()).child("volunteersNeeded").setValue(event.getVolunteersNeeded() + 1);
                                                        ref.child(eventshot.getKey()).child("numVolunteers").setValue(val);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            Toast.makeText(getApplicationContext(), "Removed from volunteer list", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.show();
                                    //if they are not signed up, ask if they would like to
                                }else{
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DonorActivity.this);
                                    builder.setCancelable(true);
                                    builder.setTitle("Volunteer for event");
                                    builder.setMessage("Would you like to sign up for this event?");

                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String eventname = event.getName();
                                            Query eventquery = ref.orderByChild("name").equalTo(eventname);
                                            eventquery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot eventshot: dataSnapshot.getChildren()){
                                                        //Toast.makeText(getApplicationContext(), eventshot.getKey(), Toast.LENGTH_LONG).show();
                                                        if (event.getVolunteers().length() == 0) {
                                                            ref.child(eventshot.getKey()).child("volunteers").setValue(event.getVolunteers() + temp);
                                                        } else {
                                                            ref.child(eventshot.getKey()).child("volunteers").setValue(event.getVolunteers()+ "," + temp);
                                                        }
                                                        ref.child(eventshot.getKey()).child("volunteersNeeded").setValue(event.getVolunteersNeeded() - 1);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    builder.show();

                                }
                                return true;

                            }
                        });

                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.donormenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ActionLogout:
                Toast.makeText(getApplicationContext(), "User Logged out", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.donate:
                startActivity(new Intent(this, DonateActivity.class));
                return true;
        }
        return true;
    }
}
