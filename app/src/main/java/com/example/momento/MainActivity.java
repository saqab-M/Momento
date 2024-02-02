package com.example.momento;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.momento.authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button btnAge;
    private TextView tvResults;
    private EditText etdDOB;
    private Button btnPick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAge = findViewById(R.id.btn_Age);
        tvResults = findViewById(R.id.tv_Result);
        etdDOB = findViewById(R.id.etd_DOB);

        etdDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        btnAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get text object
                Editable ageEditable = etdDOB.getText();

                // check if empty
                if(ageEditable != null && ageEditable.length() > 0){

                    //first convert to string
                    String strDOB = ageEditable.toString();

                    //convert string to date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    try{
                        Date dob = sdf.parse(strDOB);

                        //get current date
                        Date currDate = new Date();

                        // calculate days
                        long timeDiff = currDate.getTime() - dob.getTime();
                        long daysDiff = timeDiff / (24 * 60 * 60 * 1000); //24h 60m 60s 1000ms = ms in a day

                        int lifeExpectancyInDays = 26645- (int) daysDiff;
                        // convert to string
                        tvResults.setText(String.valueOf(lifeExpectancyInDays));

                    } catch (ParseException e){
                        // wrong input
                        tvResults.setText("Enter a valid age");
                    }

                }else {
                    // editText is empty
                    tvResults.setText("Please enter your age");
                }

            }
        });

    }

    private void showDatePickerDialog() {
        final Calendar calander = Calendar.getInstance();
        int year = calander.get(calander.YEAR);
        int month = calander.get(calander.MONTH);
        int day = calander.get(calander.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        etdDOB.setText(String.format("%02d/%02d/%d",d, m+1, y ));
                    }
                },
                year,month,day);
        datePickerDialog.show();
    }

    public void logout(View view) {
        // logout logic
        FirebaseAuth.getInstance().signOut();
        //return to register
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}