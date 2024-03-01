package com.example.momento.authentication;

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
import android.widget.EditText;
import android.widget.Spinner;

import com.example.momento.R;
import com.example.momento.main.NavMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


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
        etDOB.setOnClickListener(view -> showDatePickerDialog());


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


        // ### Upload data to firebase ###

        btnNext.setOnClickListener(view -> {

            Editable dobEdit = etDOB.getText();
            String dob = dobEdit.toString();
            double lifeExpectancy = getLifeExpectancy(selectedCountry, selectedGender);
            String deathDay = getDeathDay(dob, lifeExpectancy);

            if(selectedGender.isEmpty() || selectedCountry.isEmpty()) {
                // also check for empty dob
                // display error !!!
                // []
            }else{
                // update db
                DocumentReference docRef = db.collection("Users").document(Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
                Map<String,Object> data = new HashMap<>();
                data.put("gender", selectedGender);
                data.put("country", selectedCountry);
                data.put("DOB", dob);
                data.put("life expectancy", lifeExpectancy);
                data.put("death day", deathDay);
                data.put("init", true);
                docRef.update(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(getApplicationContext(), NavMainActivity.class));
                        finish();
                    }
                });
            }



        });

    }

    private String getDeathDay(String dob, double lifeExpectancy) {

        // parse dob
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date DOB;
        try{
            DOB = sdf.parse(dob);

            //calculate the target date
            Calendar calendar = Calendar.getInstance();
            assert DOB != null;
            calendar.setTime(DOB);

            //
            int fullYears = (int) lifeExpectancy;
            double fractionalPart = lifeExpectancy - fullYears;

            calendar.add(Calendar.YEAR, fullYears); // get from le
            calendar.add(Calendar.MONTH, (int)(fractionalPart*12)); //get from le

            return sdf.format(calendar.getTime());
        }catch (ParseException e){
            Log.d("catch", "getDeathDay: "+e.toString());
        }
        return null; // TODO check if null
    }


    private double getLifeExpectancy(String selectedCountry, String selectedGender) {
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
                    return entry.getDouble("LifeExpectancy");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d("Test123", "Error reading JSON file or parsing data");
        }

        Log.d("Test123", "No matching entry found for country: " + selectedCountry + " and gender: " + selectedGender);
        return 0.0;


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
                (datePicker, y, m, d) -> etDOB.setText(String.format("%02d/%02d/%d",d, m+1, y )),
                year,month,day);
        datePickerDialog.show();
    }

}