package com.example.momento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.momento.authentication.LoginActivity;
import com.example.momento.authentication.UserInitActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // set components
    private Button btnAge;
    private TextView tvResults;
    private EditText etdDOB;
    private Spinner spinCountries;
    private Spinner spinGender;

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;

    // declare variable
    private String selectedCountry;
    private String selectedGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAge = findViewById(R.id.btn_Age);
        tvResults = findViewById(R.id.tv_Result);
        etdDOB = findViewById(R.id.etd_DOB);
        spinCountries = findViewById(R.id.spin_Countries);
        spinGender = findViewById(R.id.spin_Gender);

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        //check is user is initialized
        userInitCheck();

        etdDOB.setFocusable(false);
        setdays();

        //read jason
        List<String> countyNames = readCountries();
        //setup spinner
        setCountriesSpinner(countyNames);
        String[] genders = {"Male", "Female", "prefer not to say"};
        setGenderSpinner(genders);

        spinGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGender = adapterView.getItemAtPosition(i).toString();
                if(selectedGender.equals("prefer not to say")){
                    selectedGender = "Both sexes";
                }
                Log.d("Test123", "selected gender: "+ selectedGender );
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCountry = adapterView.getItemAtPosition(i).toString();
                Log.d("Test123", "selected country: "+ selectedCountry );
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


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

                        int lifeExpectancyInDays = getLifeExpectancy(selectedCountry,selectedGender) - (int) daysDiff;


                        //add to database
                        DocumentReference docRef = db.collection("Users").document(fAuth.getCurrentUser().getUid());
                        Map<String,Object> data = new HashMap<>();
                        data.put("days", lifeExpectancyInDays);
                        docRef.update(data);

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



    private int getLifeExpectancy(String selectedCountry, String selectedGender) {
        try {
            // Read the JSON file
            InputStream inputStream = getResources().openRawResource(R.raw.life_expectancy_data_who);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String json = stringBuilder.toString();

            // Parse the JSON data
            JSONArray dataArray = new JSONArray(json);

            // Iterate through the data array
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject entry = dataArray.getJSONObject(i);

                String country = entry.getString("Country");
                String gender = entry.getString("Sex");

                if (country.equals(selectedCountry) && gender.equals(selectedGender)) {
                    // Retrieve life expectancy value
                    double lifeExpectancy = entry.getDouble("LifeExpectancy");
                    // get days from years
                    int fullYears = (int) lifeExpectancy;
                    double fractionalPart = lifeExpectancy - fullYears;

                    int daysInFullYear = fullYears * 365;
                    int daysInFractionalPart = (int) (fractionalPart * 365);
                    Log.d("Test123", "Years: " + fullYears + fractionalPart);

                    return daysInFullYear + daysInFractionalPart;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d("Test123", "Error reading JSON file or parsing data");
        }

        Log.d("Test123", "No matching entry found for country: " + selectedCountry + " and gender: " + selectedGender);
        return 0;
    }
    private void setGenderSpinner(String[] genderArray) {

        List<String> gendersList = new ArrayList<>(Arrays.asList(genderArray));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gendersList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.insert("gender", 0);
        spinGender.setAdapter(adapter);
    }
    private void setCountriesSpinner(List<String> countyNames) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countyNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter.insert("Select your country", 0);
        spinCountries.setAdapter(adapter);

    }
    private List<String> readCountries() {

        List<String> countryNames = new ArrayList<>();
        try{
            InputStream input = getResources().openRawResource(R.raw.countries);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            String json = new String(buffer, "UTF-8");
            JSONObject jsonObj = new JSONObject(json);
            JSONArray countriesArray = jsonObj.getJSONArray("countries");
            for (int i = 0; i < countriesArray.length(); i++) {
                countryNames.add(countriesArray.getString(i));
            }
        }catch(IOException | JSONException e){
            Log.e("CountryReader", "Error reading country names", e);
        }
        return countryNames;

    }
    private void setdays() {

        DocumentReference documentReference = db.collection("Users").document(fAuth.getCurrentUser().getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    DocumentSnapshot doc = task.getResult();
                    if(doc.contains("days")){
                        Long days = doc.getLong("days");
                        tvResults.setText(days.toString());
                    }else{
                        tvResults.setText("00");
                    }

                }
            }
        });
    }

    private void userInitCheck() {
        DocumentReference docRef = db.collection("Users").document(fAuth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                DocumentSnapshot doc = task.getResult();
                if(doc.contains("init") && doc.getBoolean("init")){
                    return;
                }else{
                    startActivity(new Intent(getApplicationContext(), UserInitActivity.class));
                    finish();
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