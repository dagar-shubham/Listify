package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.Database.DailyTask;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Task_Model;
import com.example.myapplication.Utilities.NotificationReceiver;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class UpdateDeleteTask extends AppCompatActivity {
    String[] priority_select = new String[]{"High", "Medium", "Low"};
    String deadline = "";
    String deadlineTime = "";
    Boolean isDatechanged = false;
    TextInputLayout textInputLayout;
    AutoCompleteTextView priority;
    EditText task_name, task_des;
    TextView task_deadline, task_status;
    View task_done_line;
    Task_Model task;
    ImageView delete_task, back_and_update, cancel_deadline;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete_task);

        textInputLayout = findViewById(R.id.textInputLayout);
        priority = findViewById(R.id.priority);
        task_name = findViewById(R.id.task_name);
        task_des = findViewById(R.id.task_des);
        task_deadline = findViewById(R.id.task_deadline);
        delete_task = findViewById(R.id.delete_task);
        back_and_update = findViewById(R.id.back_and_update);
        cancel_deadline = findViewById(R.id.cancel_deadline);
        task_status = findViewById(R.id.task_status);
        task_done_line = findViewById(R.id.task_done_line);

        databaseHelper = DatabaseHelper.getDB(this);

        Intent intent = getIntent();
        task = new Task_Model(intent.getIntExtra("task_id", 0), intent.getStringExtra("task_name"), intent.getStringExtra("task_des"), intent.getCharExtra("task_priority", '\0'), intent.getStringExtra("task_deadline"), intent.getBooleanExtra("isTaskCompleted", false), intent.getStringExtra("list_name"));

        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priority_select);
        priority.setAdapter(dropDownAdapter);

        if(task.getTask_priority() == 'M'){
            priority.setHint(priority_select[1]);
        } else if (task.getTask_priority() == 'H') {
            priority.setHint(priority_select[0]);
        }else{
            priority.setHint(priority_select[2]);
        }

        priority.setOnItemClickListener((parent, view, position, id) -> {
            textInputLayout.setHint("Priority");
            if(priority_select[position].equals("High")){
                task.setTask_priority('H');
            }else if(priority_select[position].equals("Medium")){
                task.setTask_priority('M');
            }else {
                task.setTask_priority('L');
            }

        });

        task_name.setText(task.getTaskName());
        task_des.setText(task.getTask_desc());
        task_deadline.setText(shortenDeadline(task.getTask_deadline()));

        if(task.getTask_deadline().equals("")) {
            cancel_deadline.setVisibility(View.GONE);
            task_deadline.setText("Select Deadline");
        }

        if(task.getTask_completed()) {
            task_status.setText("Mark Uncompleted");
            task_done_line.setVisibility(View.VISIBLE);
        }


        delete_task.setOnClickListener(v -> deleteTask());

        back_and_update.setOnClickListener(v -> backAndUpdate());

        cancel_deadline.setOnClickListener(v -> cancelDeadline());

        task_deadline.setOnClickListener(v -> setDeadline());

        task_status.setOnClickListener(v -> changeTaskStatus());

    }

    private void deleteTask() {
        databaseHelper.dailyTaskDao().deleteTask(task.getTask_id());
        cancelNotification();
        Intent intent = new Intent(this, Dash_Board.class);
        startActivity(intent);
        finishAffinity();
    }

    private void backAndUpdate() {
        if (task.getTaskName().isEmpty()) {
            // EditText field is empty, show error message
            task_name.setError("Enter task");
            // Optionally, you can request focus to the EditText field
            task_name.requestFocus();
            // Return or show appropriate message to prevent further processing
            return;
        }
        task.setTaskName(task_name.getText().toString());
        task.setTask_desc(task_des.getText().toString());
        databaseHelper.dailyTaskDao().updateTask(new DailyTask(task));
        updateNotification();
        Intent intent = new Intent(this, Dash_Board.class);
        startActivity(intent);
        finishAffinity();
    }

    private void cancelDeadline() {
        task.setTask_deadline("");
        task_deadline.setText("Select Deadline");
        cancel_deadline.setVisibility(View.GONE);
    }

    private void setDeadline() {
        Calendar calendar = Calendar.getInstance();
        deadline = "";
        deadlineTime = "";
        isDatechanged = false;

        Dialog customDatePickerDialog = new Dialog(UpdateDeleteTask.this, R.style.CustomDatePickerDialog);
        customDatePickerDialog.setContentView(R.layout.custom_date_picker_dialog);
        customDatePickerDialog.show();

        TextView set_time;
        LinearLayout task_time;
        AppCompatButton select_date, cancel_date;
        ImageView cancel_time;

        set_time = customDatePickerDialog.findViewById(R.id.set_time);
        select_date = customDatePickerDialog.findViewById(R.id.select_date);
        cancel_date = customDatePickerDialog.findViewById(R.id.cancel_date);
        task_time = customDatePickerDialog.findViewById(R.id.task_time);
        cancel_time = customDatePickerDialog.findViewById(R.id.cancel_time);

        String extractedTime = extractTime(task.getTask_deadline());

        if(!task.getTask_deadline().equals("")) {
            String[] parts = task.getTask_deadline().split(" ");
            deadline = parts[0] + " " + parts[1] + " " + parts[2];
        }

        String time = "";
        int hour, minute;
        if (extractedTime != null) {
            cancel_time.setVisibility(View.VISIBLE);
            // Extracted time is not null, proceed with timepicker setup
            String[] timeParts = extractedTime.split(":");
            hour = Integer.parseInt(timeParts[0]);
            minute = Integer.parseInt(timeParts[1]);
            time = extractedTime + " AM";

            if(task.getTask_deadline().toLowerCase().endsWith("pm")) {
                hour = timeFormat24Hr(hour);
                time = extractedTime + " PM";
            }
            deadlineTime = time;
            set_time.setText(time);

        }else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        int finalHour = hour;
        task_time.setOnClickListener(v1 -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(UpdateDeleteTask.this,
                    (view, hourOfDay, minute1) -> {
                        properTimeFormat(hourOfDay, minute1);
                        set_time.setText(deadlineTime);
                        cancel_time.setVisibility(View.VISIBLE);
                    }, finalHour, minute, false);

            timePickerDialog.show();
        });

        cancel_time.setOnClickListener(v -> {
            set_time.setText("Set Time");
            deadlineTime = "";
            cancel_time.setVisibility(View.GONE);
        });

        CalendarView datePicker = customDatePickerDialog.findViewById(R.id.datePicker);
        Calendar cal = Calendar.getInstance();

        if (!task.getTask_deadline().equals("")) {
            cal.setTime(extractDateMonth(task.getTask_deadline()));
            datePicker.setDate(cal.getTimeInMillis());
        }
        datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> properDateFormat(year, month, dayOfMonth));

        select_date.setOnClickListener(v12 -> {
            task_deadline.setText("");

            if(isDatechanged){
                deadline = deadline + " " + deadlineTime;
                task_deadline.setText(shortenDeadline(deadline));
            }else if (task.getTask_deadline().equals("")){
                Calendar calendar1 = Calendar.getInstance();
                Date curr_date = calendar1.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                deadline = dateFormat.format(curr_date) + " " + deadlineTime;
                task_deadline.setText(shortenDeadline(deadline));
            }else {
                deadline = deadline + " " + deadlineTime;
            }
            task_deadline.setText(shortenDeadline(deadline));
            task.setTask_deadline(deadline);
            cancel_deadline.setVisibility(View.VISIBLE);
            task_deadline.setHint("Select");
            customDatePickerDialog.dismiss();
        });

        cancel_date.setOnClickListener(v13 -> {
            deadline = "";
            customDatePickerDialog.dismiss();
        });
    }

    private void changeTaskStatus() {
        task.setTask_completed(!task.getTask_completed());
        if (task.getTask_completed()) {
            task_status.setText("Mark Uncompleted");
            task_done_line.setVisibility(View.VISIBLE);
        }
        else {
            task_status.setText("Mark Completed");
            task_done_line.setVisibility(View.INVISIBLE);
        }
    }

    public void properTimeFormat(int hr, int min){
        // Check if it's AM or PM
        String period;
        if (hr < 12) {
            period = "AM";
        } else {
            period = "PM";
            if (hr > 12) {
                hr -= 12; // Convert to 12-hour format
            }
        }

        // Convert hourOfDay to string (add leading zero if necessary)
        String hourString = String.format(Locale.getDefault(), "%02d", hr);

        // Convert minute to string (add leading zero if necessary)
        String minuteString = String.format(Locale.getDefault(), "%02d", min);

        // Create the time string
        deadlineTime = hourString + ":" + minuteString + " " + period;
    }

    private void properDateFormat(int year, int month, int dayOfMonth) {
        isDatechanged = true;
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Create a Calendar object and set its date to the selected date
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        // Format the date and return it as a string
        deadline = sdf.format(calendar.getTime());
    }

    public Date extractDateMonth(String dateString) {
        Date date;
        try {
            // Attempt parsing with time (for format "DD MMM 01:30 AM")
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault());
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            // If parsing with time fails, try parsing without time (for format "DD MMM")
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                date = sdf.parse(dateString);
            } catch (ParseException ex) {
                // Handle invalid format exception
                ex.printStackTrace();
                return null; // Or return appropriate value for invalid format
            }
        }
        return date;
    }
    public String shortenDeadline(String dateString){
        if(dateString.equals(""))
            return "";
        String[] parts = dateString.split(" ");
        if(parts.length == 5)
            return (parts[0] + " " + parts[1] + " " + parts[3] + " " + parts[4]);
        else
            return (parts[0] + " " + parts[1]);
    }

    public String extractTime(String dateString) {
        // Use try-catch to handle potential parsing exceptions
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault());
            Date date = sdf.parse(dateString);
            // If parsing with time format is successful, return the formatted time.
            assert date != null;
            return new SimpleDateFormat("hh:mm", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            // Handle parsing exception (likely no time in format)
            return null;
        }
    }

    public int timeFormat24Hr(int hr){
        if(hr == 12)
            return 0;
        return hr+12;
    }

    private void cancelNotification(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getTask_id(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);  // Cancel the alarm
            pendingIntent.cancel();  // Cancel the PendingIntent
        }

    }

    private void updateNotification(){
        cancelNotification();

        // Set a new newNotification with updated data
        if (task.getTask_deadline().equals("") || task.getTask_completed() || isCurrentDateGreaterThan(parseDate(task.getTask_deadline()))) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(parseDate(task.getTask_deadline())));

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("task_description", task.getTask_desc());
        intent.putExtra("notification_id", task.getTask_id());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getTask_id(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        long alarmTimeMillis = calendar.getTimeInMillis();

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
            }
        }
    }

    private boolean isCurrentDateGreaterThan(Date givenDate) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Compare the dates
        return currentDate.after(givenDate);
    }

    public static Date parseDate(String dateString) {
        // Define date formats
        SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault());

        try {
            // Try to parse the date string with time
            return dateFormatWithTime.parse(dateString);

        } catch (ParseException e) {
            try {
                String dateWithTimeString = dateString + " 08:00 AM";
                return dateFormatWithTime.parse(dateWithTimeString);
            } catch (ParseException ex) {
                // Handle exception if both parsing attempts fail
                ex.printStackTrace();
                return null;
            }
        }
    }

}