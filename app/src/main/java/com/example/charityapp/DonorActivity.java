package com.example.charityapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description main screen for donors where they can view all the events
 *
 * @authors Jack Bates, AJ Thut
 * @date_created 2/15/20
 * @date_modified 5/4/20
 */
public class DonorActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference ref, vol;
    FirebaseDatabase database;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor);

        //set up the custom tool bar
        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        //set up the recycle viewer for displaying the events
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //create a database reference for the events
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Events");
        //create a user instance for the current user
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Event, recycleAdapter> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, recycleAdapter>(Event.class, R.layout.row, recycleAdapter.class, ref){
                    protected void populateViewHolder(recycleAdapter holder, Event event, int i){
                        holder.setView(getApplicationContext(), event.getName(), event.getProgram(), event.getDate(), event.getTime());
                        //on click listener for each event in the list
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //create an intent to the detail page of the clicked event
                                Intent intent = new Intent(DonorActivity.this, DonorEventDetails.class);
                                //put all the events details in the intent
                                intent.putExtra("Name", event.getName());
                                intent.putExtra("Program", event.getProgram());
                                intent.putExtra("Description", event.getDescription());
                                intent.putExtra("Date", event.getDate());
                                intent.putExtra("Time", event.getTime());
                                intent.putExtra("Funds", event.getFunding());
                                intent.putExtra("Volunteers", event.getVolunteers());
                                intent.putExtra("VolunteersNeeded", event.getVolunteersNeeded());
                                //start the intent
                                startActivity(intent);
                            }
                        });
                        //long click listener for each event
                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DonorActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("Contribute To Event");
                                builder.setMessage("Would you like to sign up or donate to this event?");
                                //make a donate button in the pop up
                                builder.setPositiveButton("Donate", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //create an intent to the detail page of the clicked event
                                        Intent intent = new Intent(DonorActivity.this, DonorEventDetails.class);
                                        //put all the events details in the intent
                                        intent.putExtra("Name", event.getName());
                                        intent.putExtra("Program", event.getProgram());
                                        intent.putExtra("Description", event.getDescription());
                                        intent.putExtra("Date", event.getDate());
                                        intent.putExtra("Time", event.getTime());
                                        intent.putExtra("Funds", event.getFunding());
                                        intent.putExtra("Volunteers", event.getVolunteers());
                                        intent.putExtra("VolunteersNeeded", event.getVolunteersNeeded());
                                        //start the intent
                                        startActivity(intent);
                                    }
                                });
                                //make a volunteer button in the popup
                                builder.setNegativeButton("Volunteer", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //get inst of the current user
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        //remove donor from username
                                        String temp = user.getDisplayName().replaceAll("Donor:", "");
                                        ValueEventListener eventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                //Log.d("DonorActivity", "Number of events" + Long.toString(dataSnapshot.getChildrenCount()));
                                                //get all of the events into a list
                                                Iterable<DataSnapshot> events = dataSnapshot.getChildren();
                                                int[] two_events = {0};
                                                int count = 0;
                                                //loop through all of the events
                                                for (DataSnapshot s : events) {
                                                    if (event.getName().equals(s.getKey())) {
                                                        //do nothing
                                                    } else {
                                                        String month = s.child("date").getValue(String.class);
                                                        String[] compareevent = month.split("\\/");
                                                        //Log.d("split the date", comapareevent[2]);
                                                        String[] eventdetails = event.getDate().split("\\/");
                                                        //check if the times overlap, first start with year, then month, then day
                                                        if (eventdetails[2].equals(compareevent[2])) {
                                                            if (eventdetails[0].equals(compareevent[0])) {
                                                                if (eventdetails[1].equals(compareevent[1])) {
                                                                    String vols = s.child("volunteers").getValue(String.class);
                                                                    //if the donor has volunteered for an event on the same day already
                                                                    if (vols.contains(temp)) {
                                                                        String[] comparetimes = s.child("time").getValue(String.class).split("-");
                                                                        String[] times = event.getTime().split("-");
                                                                        Date start1 = null;
                                                                        Date start2 = null;
                                                                        Date end1 = null;
                                                                        Date end2 = null;
                                                                        //get the times into a format that can be used by Date object
                                                                        for (int i = 0; i < comparetimes.length; i++) {
                                                                            SimpleDateFormat mformat = new SimpleDateFormat("HH:mm");
                                                                            SimpleDateFormat oldformat = new SimpleDateFormat("hh:mma");
                                                                            Date date = null;
                                                                            try {
                                                                                date = oldformat.parse(comparetimes[i]);
                                                                            } catch (ParseException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            comparetimes[i] = mformat.format(date);
                                                                            if (i == 0)
                                                                                start1 = date;
                                                                            if (i == 1) end1 = date;
                                                                        }
                                                                        //get time for other event into format for Date object
                                                                        for (int i = 0; i < times.length; i++) {
                                                                            SimpleDateFormat mformat = new SimpleDateFormat("HH:mm");
                                                                            SimpleDateFormat oldformat = new SimpleDateFormat("hh:mma");
                                                                            Date date = null;
                                                                            try {
                                                                                date = oldformat.parse(times[i]);
                                                                            } catch (ParseException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            times[i] = mformat.format(date);
                                                                            if (i == 0)
                                                                                start2 = date;
                                                                            if (i == 1) end2 = date;
                                                                        }
                                                                        //check if the two times overlap with eachother
                                                                        if ((null == end2 || start1.before(end2)) && (null == end1 || start2.before(end1))) {
                                                                            two_events[0] = 1;
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }

                                                    }
                                                }
                                                count++;
                                                //display the correct message for user
                                                displayMessages(two_events[0], count, event, temp, user);
                                                two_events[0] = 0;
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        };
                                        ref.addListenerForSingleValueEvent(eventListener);

                                    }
                                });
                                //make a cancle button in the pop up
                                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //close the pop up when clicked
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });

                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu in the top right corner of toolbar
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.donormenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //switch statement for when each menu item is selected
        switch (item.getItemId()){
            //logout button
            case R.id.ActionLogout:
                //tell user they logged out and sign them out from firebase
                Toast.makeText(getApplicationContext(), "User Logged out", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.donate:
                //button for unrestricted donation
                startActivity(new Intent(this, DonateActivity.class));
                return true;
            //help button
            case R.id.ActionHelp:
                //send the user to the help screen
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }
        return true;
    }

    /**
     * Displays the correct message to the user
     * @param equals
     * @param count
     * @param event
     * @param temp
     * @param user
     */
    private void displayMessages(int equals, int count, Event event, String temp, FirebaseUser user){
        //only display the message once
        if(count <= 1){
            //tell user they can't sign up for multiple events in one day
            if (equals == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DonorActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Volunteering for Multiple Events");
                builder.setMessage("You cannot volunteer for multiple events at the same time.");

                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                //tell user the volunteer list is full
            } else if (event.getVolunteersNeeded() <= 0 && !event.getVolunteers().contains(temp)) {
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
                //ask user if they want to cancel their registration if they are already signed up
            } else if (event.getVolunteers().contains(temp)) {
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
                        //get the name of the event and query of the event
                        String eventname = event.getName();
                        Query eventquery = ref.orderByChild("name").equalTo(eventname);
                        eventquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //remove the name of the donor from the list of volunteers
                                for (DataSnapshot eventshot : dataSnapshot.getChildren()) {
                                    String original = event.getVolunteers();
                                    String newVolList;
                                    if (event.getVolunteers().length() <= temp.length() + 1) {
                                        newVolList = original.replace(temp, "");
                                    } else if (event.getVolunteers().substring(0, temp.length()).equals(temp)) {
                                        newVolList = original.replace(temp + ",", "");
                                    } else {
                                        newVolList = original.replace("," + temp, "");
                                    }
                                    //return the new string of volunteers and change the number of volunteers
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
                        //get refrence to donor data in the database
                        vol = FirebaseDatabase.getInstance().getReference("DonorDetails").child(user.getDisplayName().replace("Donor:", "")).child("hours");
                        vol.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int hours = dataSnapshot.getValue(Integer.class);
                                String[] comparetimes = event.getTime().split("-");
                                Log.d("times", comparetimes[0]);
                                String[] times = event.getTime().split("-");
                                Date start1 = null;
                                Date end1 = null;
                                //comparing times of the start/end time of the event
                                for(int i = 0; i < comparetimes.length; i++){
                                    SimpleDateFormat mformat = new SimpleDateFormat("HH:mm");
                                    SimpleDateFormat oldformat = new SimpleDateFormat("hh:mma");
                                    Date date = null;
                                    try {
                                        date = oldformat.parse(comparetimes[i]);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    comparetimes[i] = mformat.format(date);
                                    if(i == 0) start1 = date;
                                    if(i == 1) end1 = date;
                                }
                                //get the time in milliseconds, convert it to hours
                                long start = start1.getTime();
                                long end = end1.getTime();
                                long result = end - start;
                                result = result / 3600000;
                                hours  -= result;
                                //set number of hours volunteered for donor in database
                                vol.setValue(hours);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                builder.show();
                //if they are not signed up, ask if they would like to
            } else {
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
                //add donor to list of volunteers
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String eventname = event.getName();
                        Query eventquery = ref.orderByChild("name").equalTo(eventname);
                        eventquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot eventshot : dataSnapshot.getChildren()) {
                                    //Toast.makeText(getApplicationContext(), eventshot.getKey(), Toast.LENGTH_LONG).show();
                                    if (event.getVolunteers().length() == 0) {
                                        ref.child(eventshot.getKey()).child("volunteers").setValue(event.getVolunteers() + temp);
                                    } else {
                                        ref.child(eventshot.getKey()).child("volunteers").setValue(event.getVolunteers() + "," + temp);
                                    }
                                    ref.child(eventshot.getKey()).child("volunteersNeeded").setValue(event.getVolunteersNeeded() - 1);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Toast.makeText(getApplicationContext(), "Signed Up", Toast.LENGTH_LONG).show();
                        //same as above for removing the donor from volunteers list but this time adding hours instead of deducting them
                        vol = FirebaseDatabase.getInstance().getReference("DonorDetails").child(user.getDisplayName().replace("Donor:", "")).child("hours");
                        vol.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int hours = dataSnapshot.getValue(Integer.class);
                                String[] comparetimes = event.getTime().split("-");
                                Log.d("times", comparetimes[0]);
                                String[] times = event.getTime().split("-");
                                Date start1 = null;
                                Date end1 = null;

                                for(int i = 0; i < comparetimes.length; i++){
                                    SimpleDateFormat mformat = new SimpleDateFormat("HH:mm");
                                    SimpleDateFormat oldformat = new SimpleDateFormat("hh:mma");
                                    Date date = null;
                                    try {
                                        date = oldformat.parse(comparetimes[i]);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    comparetimes[i] = mformat.format(date);
                                    if(i == 0) start1 = date;
                                    if(i == 1) end1 = date;
                                }

                                long start = start1.getTime();
                                long end = end1.getTime();
                                long result = end - start;
                                result = result / 3600000;
                                hours  += result;

                                vol.setValue(hours);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                builder.show();

            }
        }
    }
}
