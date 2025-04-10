package com.example.healthcare;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    EditText medName;
    TimePicker timePicker;
    Button setReminderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        medName = findViewById(R.id.medName);
        timePicker = findViewById(R.id.timePicker);
        setReminderBtn = findViewById(R.id.setReminderBtn);

        timePicker.setIs24HourView(true);

        setReminderBtn.setOnClickListener(v -> {
            try {
                setMedicationReminder();
            } catch (Exception e) {
                Toast.makeText(ReminderActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setMedicationReminder() {
        String name = medName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medication name", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int hour, minute;

        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(this, ReminderBroadcast.class);
        intent.putExtra("medName", name);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                permissionIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(permissionIntent);
                Toast.makeText(this, "Please allow exact alarm permission", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Reminder set for " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "AlarmManager error!", Toast.LENGTH_SHORT).show();
        }
    }
}
