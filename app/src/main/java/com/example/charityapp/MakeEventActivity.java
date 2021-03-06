//Activity to create a new event

package com.example.charityapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @description activity where admin creates a new event
 *
 * @authors Jack Bates, Felix Estrella, AJ Thut
 * @date_created 02/12/20
 * @date_modified 05/03/20
 */
public class MakeEventActivity extends AppCompatActivity {

    FirebaseDatabase mFirebasedatabase;
    DatabaseReference mRefrence, dataRefrence;

    EditText Name, Program, Description, VolsNeeded;
    TextView dateView, timeView;
    int maxId = 0;
    Button btn, Date, TimeStart, TimeEnd;
    Button cancelBtn;
    String startTime, EndTime;
    Event event;
    double militaryStartTimeDecimal = 0;
    double militaryEndTimeDecimal = 0;
    double startHours = 0;
    double startMin = 0;
    double endMin = 0;
    double endHours = 0;
    String startAmOrPm = "";
    String endAmOrPm = "";

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_event);

        Name = findViewById(R.id.name_edit);
        Program = findViewById(R.id.program_edit);
        Description = findViewById(R.id.description_edit);
        Date = findViewById(R.id.dateBtn);
        TimeStart = findViewById(R.id.timeStartBtn);
        TimeEnd = findViewById(R.id.timeStopBtn);
        dateView = findViewById(R.id.dateViewTxt);
        timeView = findViewById(R.id.timeViewTxt);
        VolsNeeded = findViewById(R.id.volsNeeded_edit);
        btn = findViewById(R.id.create_btn);
        cancelBtn = findViewById(R.id.cancel_btn);

        startTime = "00:00";
        EndTime = "00:00";


        event = new Event();

        mRefrence = FirebaseDatabase.getInstance().getReference("Events");
        dataRefrence = FirebaseDatabase.getInstance().getReference("Data");
        //setting time for the event
        TimeStart.setOnClickListener(new View.OnClickListener() {
            Calendar cal = Calendar.getInstance();
            //get hours/min for the event start/end
            int hour = cal.get(Calendar.HOUR);
            int minute = cal.get(Calendar.MINUTE);
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MakeEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String m;
                        String minString;
                        int tempHours = hourOfDay;
                        //get the hour of day for event
                        if (hourOfDay == 0) {
                            hourOfDay += 12;
                            m = "AM";
                        }
                        else if (hourOfDay == 12) {
                            m = "PM";
                        }
                        else if (hourOfDay > 12) {
                            hourOfDay -= 12;
                            m = "PM";
                        }
                        else {
                            m = "AM";
                        }
                        if(minute < 10){
                            minString = "0" + minute;
                        } else{
                            minString = "" + minute;
                        }
                        //set the time for event
                        startTime = hourOfDay + ":" + minString + m;
                        timeView.setText(startTime + " - " + EndTime);
                        startAmOrPm = m;
                        startHours = hourOfDay;
                        startMin = minute;
                    }
                }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        //same as code above but for end time for event
        TimeEnd.setOnClickListener(new View.OnClickListener() {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR);
            int minute = cal.get(Calendar.MINUTE);
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MakeEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String m;
                        String minString;
                        if (hourOfDay == 0) {
                            hourOfDay += 12;
                            m = "AM";
                        }
                        else if (hourOfDay == 12) {
                            m = "PM";
                        }
                        else if (hourOfDay > 12) {
                            hourOfDay -= 12;
                            m = "PM";
                        }
                        else {
                            m = "AM";
                        }
                        if(minute < 10){
                            minString = "0" + minute;
                        } else{
                            minString = "" + minute;
                        }
                        EndTime = hourOfDay + ":" + minString + m;
                        timeView.setText(startTime + " - " + EndTime);
                        endAmOrPm = m;
                        endHours = hourOfDay;
                        endMin = minute;
                    }
                }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        //get the date for the event from date object
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                //show the date window to the user
                DatePickerDialog dialog = new DatePickerDialog(MakeEventActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                String date = month + "/" + dayOfMonth + "/" + year;

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date exitdate = null;
                try {
                    exitdate = df.parse(year + "-" + month + "-" + dayOfMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //checks that the data is in the future
                Date currdate = new Date();
                long diff = currdate.getTime() - exitdate.getTime();
                if(diff > 86400000){
                    Toast.makeText(getApplicationContext(), "Please enter future date", Toast.LENGTH_LONG).show();
                } else{
                    dateView.setText(date);
                }
            }
        };
        //converts times from above to military time
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startAmOrPm.equals("AM")) {
                    if (startHours == 12) {
                        militaryStartTimeDecimal = startHours - 12 + (startMin / 60);
                    } else {
                        militaryStartTimeDecimal = startHours + (startMin / 60);
                    }
                } else {
                    if (startHours == 12) {
                        militaryStartTimeDecimal = startHours + (startMin / 60);
                    } else {
                        militaryStartTimeDecimal = startHours + 12 + (startMin / 60);
                    }
                }
                if (endAmOrPm.equals("AM")) {
                    if (endHours == 12) {
                        militaryEndTimeDecimal = endHours - 12 + (endMin / 60);
                    } else {
                        militaryEndTimeDecimal = endHours + (endMin / 60);
                    }
                } else {
                    if (endHours == 12) {
                        militaryEndTimeDecimal = endHours + (endMin / 60);
                    } else {
                        militaryEndTimeDecimal = endHours + 12 + (endMin / 60);
                    }
                }
                //set all of the data needed for the event into an event object
                if (militaryEndTimeDecimal - militaryStartTimeDecimal <= 0) {
                    Toast.makeText(getApplicationContext(), "Please make sure the time for start and end are possible", Toast.LENGTH_LONG).show();
                } else if (!Name.getText().toString().equals("")
                        && !Program.getText().toString().equals("")
                        && !Description.getText().toString().equals("")
                        && !dateView.getText().toString().equals("MM/DD/YYYY")
                        && !startTime.equals("00:00")
                        && !EndTime.equals("00:00")
                        && !VolsNeeded.getText().toString().equals("")){
                    event.setName(Name.getText().toString());
                    event.setProgram(Program.getText().toString());
                    event.setDescription(Description.getText().toString());
                    event.setDate(dateView.getText().toString());
                    event.setTime(timeView.getText().toString());
                    event.setFunding(0);
                    event.setVolunteers("");
                    event.setVolunteersNeeded(Integer.parseInt(VolsNeeded.getText().toString()));
                    event.setNumVolunteers(0);
                    //create the event in the database
                    mRefrence.child(event.getName()).setValue(event);
                    dataRefrence.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //change values for organization analytics
                            int value = dataSnapshot.child("numEvents").getValue(Integer.class);
                            dataRefrence.child("numEvents").setValue(value + 1);
                            int value2 = dataSnapshot.child("totNumEvents").getValue(Integer.class);
                            dataRefrence.child("totNumEvents").setValue(value2 + 1);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Code
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Event Created", Toast.LENGTH_LONG).show();

                    finish();
                    Intent intent = new Intent(MakeEventActivity.this, HomeActivity.class);
                    startActivity(intent);

                } else{
                    Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_LONG).show();
                }

            }
        });
        //if user decides to cancel creating the event
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MakeEventActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }



}
