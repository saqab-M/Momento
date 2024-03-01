package com.example.momento.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


import com.example.momento.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class CountDownWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //set up alarm to trigger update at midnight
        setMidnightUpdateAlarm(context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void setMidnightUpdateAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CountDownWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        // set alarm to Start at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today's midnight, schedule the alarm for the next day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        //schedule the alarm to trigger every day at midnight
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.count_down_widget);

        // get user id
        String uID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        
        getDaysDataFromFirestore(context, uID, views, appWidgetManager,appWidgetId);
        
    }


    private static void getDaysDataFromFirestore(Context context,  String userID, RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {

        //get data from fire store
        DocumentReference docref = FirebaseFirestore.getInstance().collection("Users").document(userID);

        docref.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                DocumentSnapshot docSnap = task.getResult();

                if (docSnap.exists() && docSnap.contains("DOB") && docSnap.contains("life expectancy")){
                    double lifeExpectancy = docSnap.getDouble("life expectancy");
                    String dobUser = docSnap.getString("DOB");

                    String daysLeft = getDaysLeft(lifeExpectancy, dobUser);
                    //update widget
                    views.setTextViewText(R.id.appwidget_text, daysLeft);
                }else{
                    views.setTextViewText(R.id.appwidget_text, "Momento");
                }

            }else{
                views.setTextViewText(R.id.appwidget_text, "Error!");
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    private static String getDaysLeft(double lifeExpectancy, String dobUser) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date dob = sdf.parse(dobUser);

            Date currentDate = new Date();

            //calculate days
            assert dob != null;
            long timeDiff = currentDate.getTime() - dob.getTime();
            long daysDiff = timeDiff / (24 * 60 * 60 *1000); // //24h 60m 60s 1000ms = ms in a day


            int fullYears = (int) lifeExpectancy;
            double fractionalPart = lifeExpectancy - fullYears;
            int daysInFullYear = fullYears * 365;
            int daysInFractionalPart = (int) (fractionalPart * 365);
            int totalLifeExpectancyInDays = daysInFullYear + daysInFractionalPart;

            return String.valueOf(totalLifeExpectancyInDays - (int)daysDiff);


        } catch (ParseException e) {
            Log.d("catch", "getDaysLeft: error!");
            return "00";
        }

    }


}