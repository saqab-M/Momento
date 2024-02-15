package com.example.momento.authentication;

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

import com.example.momento.MainActivity;
import com.example.momento.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInitActivity extends AppCompatActivity {

    // set components
    private EditText etDOB;
    private Spinner spinGender;
    private Spinner spinCountry;
    private Button btnNext;

    //set firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;


    // set Variables
    private String selectedGender = "";
    private String selectedCountry = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_init);

        //init components
        etDOB = findViewById(R.id.et_DOB);
        spinGender = findViewById(R.id.spin_Gender);
        spinCountry = findViewById(R.id.spin_Countries);
        btnNext = findViewById(R.id.btn_Next);
        //init firebase
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //get data
        List<String> countryNames = readCountries();
        String[] genders = {"Male", "Female", "prefer not to say"};
        //set spinner
        setCountriesSpinner(countryNames);
        setGenderSpinner(genders);

        etDOB.setFocusable(false);
        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });


        // ### item selection ###
        spinGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = adapterView.getItemAtPosition(i).toString();
                if(!selection.equals("Gender")){
                    if(selection.equals("prefer not to say")){
                        selection = "Both sexes";
                    }
                    selectedGender = selection;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing
            }

        });
        spinCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = adapterView.getItemAtPosition(i).toString();
                if(!selection.equals("Select your country")){
                    selectedCountry = selection;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // ### ---------- ###

        btnNext.setOnClickListener(view -> {

            Editable dobEdit = etDOB.getText();
            String dob = dobEdit.toString();

            if(selectedGender.isEmpty() || selectedCountry.isEmpty()) {
                // also check for empty dob
                // display error !!!
                // []
            }else{
                // update db
                DocumentReference docRef = db.collection("Users").document(fAuth.getCurrentUser().getUid());
                Map<String,Object> data = new HashMap<>();
                data.put("gender", selectedGender);
                data.put("country", selectedCountry);
                data.put("DOB", dob);
                data.put("init", true);
                docRef.update(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
            }



        });

    }

    private void setCountriesSpinner(List<String> countryNames) {
        //populate spinner with country names

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter.insert("Select your country", 0);
        spinCountry.setAdapter(adapter);

    }

    private void setGenderSpinner(String[] genders) {
        //populate spinner with genders

        List<String> gendersList = new ArrayList<>(Arrays.asList(genders));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gendersList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.insert("Gender", 0);
        spinGender.setAdapter(adapter);
    }

    private List<String> readCountries() {
        //read json file and create a list of objects

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
            // ### set log #####
            Log.e("CountryReader", "Error reading country names", e);
        }

        return countryNames;
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
                        etDOB.setText(String.format("%02d/%02d/%d",d, m+1, y ));
                    }
                },
                year,month,day);
        datePickerDialog.show();
    }

}