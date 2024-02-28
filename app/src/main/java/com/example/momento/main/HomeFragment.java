package com.example.momento.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.momento.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HomeFragment extends Fragment {

    // init components
    private TextView tvLifeExpectancy;
    private TextView tvRemainingLE;
    private TextView tvPercentage;
    private TextView tvDeathDay;

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // find components
        tvLifeExpectancy = rootView.findViewById(R.id.tv_lifeExpectancy);
        tvRemainingLE = rootView.findViewById(R.id.tv_remainingLE);
        tvPercentage = rootView.findViewById(R.id.tv_percentage);
        tvDeathDay = rootView.findViewById(R.id.tv_deathDay);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setUserData();







        // Inflate the layout for this fragment
        return rootView;
    }

    private void setUserData() {

        DocumentReference docRef= db.collection("Users").document(fAuth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.contains("life expectancy") && doc.contains("DOB")){
                    double lifeExpectancy = doc.getDouble("life expectancy");
                    String dobUser = doc.getString("DOB");

                    // # life expectancy #
                    int years = (int)lifeExpectancy;
                    int months = (int)((lifeExpectancy - years) * 12);
                    tvLifeExpectancy.setText(years + " Years " + months + " Months");
                    //TODO make this more accurate calculate days

                    // # days left #
                    String daysLeft = getDaysLeft(lifeExpectancy, dobUser);
                    tvRemainingLE.setText(daysLeft);


                    // # percentage #
                    String percentageLived = getPercentage(lifeExpectancy, dobUser);
                    tvPercentage.setText(percentageLived + " %");

                }
                if(doc.contains("death day")){
                    String deathDay = doc.getString("death day");
                    tvDeathDay.setText(deathDay);
                }



            }
        });
    }

    private String getDaysLeft(double lifeExpectancy, String dobUser) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date dob = sdf.parse(dobUser);

            Date currentDate = new Date();

            //calculate days
            Long timeDiff = currentDate.getTime() - dob.getTime();
            long daysDiff = timeDiff / (24 * 60 * 60 *1000); // //24h 60m 60s 1000ms = ms in a day


            int fullYears = (int) lifeExpectancy;
            double fractionalPart = lifeExpectancy - fullYears;
            int daysInFullYear = fullYears * 365;
            int daysInFractionalPart = (int) (fractionalPart * 365);
            int totalLifeExpectancyInDays = daysInFullYear + daysInFractionalPart;

            return String.valueOf(totalLifeExpectancyInDays - (int)daysDiff);


        } catch (ParseException e) {
            e.printStackTrace();
            return "00";
        }

    }

    private String getPercentage(double lifeExpectancy, String dobUser) {
        //total days
        int totaldays = (int)(lifeExpectancy * 365.25); // TODO make this more accurate using death day or calendar
        // format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dob;
        try{
            dob = sdf.parse(dobUser);

            //calculate the number of days lived
            Date currentDate = new Date();
            long diffInMillies = Math.abs(currentDate.getTime() - dob.getTime());
            long daysLived = diffInMillies / (1000 * 60 * 60 * 24); // convert millisec to days

            //calculate percentage
            double percentageLived = (double) daysLived / totaldays * 100;

            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(percentageLived);

        } catch (ParseException e) {
            e.printStackTrace();
            return "0.00";
        }
    }
}