package com.example.momento.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.example.momento.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 */
public class CountDownWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.count_down_widget);
        //views.setTextViewText(R.id.appwidget_text, "Momento");

        // get user id
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        getDaysDataFromFirestore(context, uID, views, appWidgetManager,appWidgetId);
        
    }




    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void getDaysDataFromFirestore(Context context,  String userID, RemoteViews views, AppWidgetManager appWidgetManager, int appWidgetId) {

        //get data from fire store
        DocumentReference docref = FirebaseFirestore.getInstance().collection("Users").document(userID);

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    DocumentSnapshot docSnap = task.getResult();

                    if (docref != null && docSnap.exists() && docSnap.contains("days")){
                        long days = docSnap.getLong("days");

                        //update widget
                        views.setTextViewText(R.id.appwidget_text, String.valueOf(days));
                    }else{
                        views.setTextViewText(R.id.appwidget_text, "Momento");
                    }

                }else{
                    views.setTextViewText(R.id.appwidget_text, "Error!");
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }


}