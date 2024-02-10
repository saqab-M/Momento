package com.example.momento.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.momento.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserInitActivity extends AppCompatActivity {

    // set components
    private EditText etDOB;
    private Spinner spinGender;
    private Spinner spinCountry;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_init);

        //init components
        etDOB = findViewById(R.id.etd_DOB);
        spinGender = findViewById(R.id.spin_Gender);
        spinCountry = findViewById(R.id.spin_Countries);
        btnNext = findViewById(R.id.btn_Next);


        //etDOB.setFocusable(false); crash

        //read jason

        List<String> countryNames = readCountries();
        String[] genders = {"Male", "Female", "prefer not to say"};

        //set spinner

        setCountriesSpinner(countryNames);
        setGenderSpinner(genders);

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
        adapter.insert("gender", 0);
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

}