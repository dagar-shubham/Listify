package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Database.DailyTask;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Task_Model;
import com.example.myapplication.Utilities.NotificationReceiver;
import com.example.myapplication.R;
import com.example.myapplication.Fragments.ToDoFragment;
import com.example.myapplication.Adapters.ToDoPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Dash_Board extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    Boolean configurationChanged = false;
    Boolean isNotificationAllowed = false;
    int[] destroyed_frags;
    String[] priority_select = new String[]{"High", "Medium", "Low"};
    String deadline = "";
    String deadlineTime = "";
    Boolean isDatechanged = false;
    Integer currFragPos;
    SharedPreferences cpref;
    SharedPreferences.Editor ceditor;
    DatabaseHelper databaseHelper;
    ToDoFragment currFrag;
    AppCompatImageButton add_task;
    ArrayList<String> tabNames = new ArrayList<>();
    TextView day_date, createNewList;
    AppCompatImageView task_menu, sort_tasks;
    ViewPager2 task_viewPager;
    TabLayout tabLayout;
    HorizontalScrollView horizontalScrollView;
    ToDoPagerAdapter adapter;
    List<ToDoFragment> fragmentList = new ArrayList<>();
    ArrayList<DailyTask> arrDailyTask = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        if(savedInstanceState != null){
            configurationChanged = true;
            currFragPos = savedInstanceState.getInt("currFragPos");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app needs the Exact Alarm permission to send notifications at exact time. Please enable it in the app settings.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            // Open app settings
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setIcon(R.drawable.app_icon)
                        .show();
            }
        }

        userLoggedIn();              // to make sure that user will not see onboarding process again

        getAllTasksFromDB();         // to fetch all tasks from database if already exists

        initializeIDs();             // to find IDs of different views

        getTodayDayDate();           // to get Today's day and date and display it

        requestNotificationPermission();

        if(!configurationChanged) {
            getInitialTabs();            // to make visible all previously stored lists in the app

            syncTabWithFragments();      // to create a tab layout mediator which will make sure tab and fragments synchronization

            setLastVisitedTab();         // to select the last visited tab

            tabSelectedListener();       // to create tab selected listener which will trigger when a tab is selected
        }

        createNewList.setOnClickListener(v -> addNewFragment());

        add_task.setOnClickListener(v1 -> addNewTask());

        task_menu.setOnClickListener(v -> openTaskMenu());

        sort_tasks.setOnClickListener(v -> sortTasks());


    }

    @Override
    protected void onStart() {
        super.onStart();

        // onStart is implemented so that those fragments which are automatically recreated
        // due to configuration change, they triggerred their onCreate method and restored fragment id
        // now we will using that id to sequentially add fragments to the fragment list

        if(configurationChanged) {
            getInitialTabs();            // to make visible all previously stored lists in the app

            syncTabWithFragments();      // to create a tab layout mediator which will make sure tab and fragments synchronization

            setLastVisitedTab();         // to select the last visited tab

            tabSelectedListener();       // to create tab selected listener which will trigger when a tab is selected

            configurationChanged = false;
        }

    }

    private void userLoggedIn(){
        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("flag", true).apply();
    }

    private void getAllTasksFromDB(){
        databaseHelper = DatabaseHelper.getDB(this);
        arrDailyTask = (ArrayList<DailyTask>) databaseHelper.dailyTaskDao().getAllTasks();
    }

    private void initializeIDs(){
        day_date = findViewById(R.id.day_date);
        task_menu = findViewById(R.id.task_menu);
        task_viewPager = findViewById(R.id.task_viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        createNewList = findViewById(R.id.creteNewList);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        add_task = findViewById(R.id.add_task);
        sort_tasks = findViewById(R.id.sort_tasks);
    }

    private void getTodayDayDate(){
        Calendar calendar = Calendar.getInstance();
        Date curr_date = calendar.getTime();
        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.getDefault());
        String dayOfWeek = dayFormat.format(curr_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        String date = dateFormat.format(curr_date);
        String today = dayOfWeek + " " + date;

        day_date.setText(today);
    }

    public void getInitialTabs(){
        boolean is_frag_present;

        tabNames = (ArrayList<String>) databaseHelper.dailyTaskDao().getAllLists();

        if (configurationChanged) {
            ToDoFragment frag;
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            destroyed_frags = new int[fragments.size()];
            for(int i = 0; i<tabNames.size(); i++){
                for(int j=0; j<fragments.size(); j++){
                    frag = (ToDoFragment) fragments.get(j);
                    if(i == frag.getFragId()) {
                        fragmentList.add(frag);
                    }
                }
            }
            for(int i=0; i<fragmentList.size(); i++){
                destroyed_frags[i] = fragmentList.get(i).getFragId();
            }
        }

        if(tabNames.isEmpty()){
            ArrayList<Task_Model> arrTask = new ArrayList<>();
            ArrayList<Task_Model> arrTaskCompleted = new ArrayList<>();
            tabNames.add("TO-DO LIST");
            databaseHelper.dailyTaskDao().addTask(new DailyTask("", "", '\0', "", false, tabNames.get(0), 1));
            ToDoFragment newFragment = new ToDoFragment(0, arrTask, arrTaskCompleted, databaseHelper, tabNames.get(0), 1);
            fragmentList.add(newFragment);
        }
        else{
            for(int i=0; i<tabNames.size(); i++){
                is_frag_present = false;

                if(configurationChanged) {
                    for (int destroyedFrag : destroyed_frags) {
                        if (destroyedFrag == i) {
                            is_frag_present = true;
                            break;
                        }
                    }
                }

                if (is_frag_present)
                    continue;

                ArrayList<Task_Model> arrTask = new ArrayList<>();
                ArrayList<Task_Model> arrTaskCompleted = new ArrayList<>();
                for (int j = 0; j < arrDailyTask.size(); j++) {
                    if (String.valueOf(arrDailyTask.get(j).getListName()).equals(tabNames.get(i)) && arrDailyTask.get(j).getTaskPriority() != '\0') {
                        if(!arrDailyTask.get(j).getTaskCompleted()) {
                            //if task is not completed
                               Task_Model temp = new Task_Model(arrDailyTask.get(j).getId(), arrDailyTask.get(j).getTaskName(), arrDailyTask.get(j).getTaskDesc(), arrDailyTask.get(j).getTaskPriority(), arrDailyTask.get(j).getTaskDateTime(), arrDailyTask.get(j).getTaskCompleted(), arrDailyTask.get(j).getListName());
                               arrTask.add(temp);
                            }
                        else{
                            //if task is completed
                            Task_Model temp = new Task_Model(arrDailyTask.get(j).getId(), arrDailyTask.get(j).getTaskName(), arrDailyTask.get(j).getTaskDesc(), arrDailyTask.get(j).getTaskPriority(), arrDailyTask.get(j).getTaskDateTime(), arrDailyTask.get(j).getTaskCompleted(), arrDailyTask.get(j).getListName());
                            arrTaskCompleted.add(temp);
                        }
                    }
                }

                ToDoFragment newFragment;
                int sortby = databaseHelper.dailyTaskDao().getSortBy(tabNames.get(i), '\0');

                newFragment = new ToDoFragment(i, arrTask, arrTaskCompleted, databaseHelper, tabNames.get(i), sortby);
                fragmentList.add(i, newFragment);

            }
        }

        adapter = new ToDoPagerAdapter(Dash_Board.this, fragmentList);
        task_viewPager.setAdapter(adapter);

    }

    private void setLastVisitedTab(){
        cpref = getSharedPreferences("IdentifyCurrTab", MODE_PRIVATE);
        ceditor = cpref.edit();
        int lastTabPos = cpref.getInt("tab",0);
        int storedPos = cpref.getInt("scroll", 0);
        if(lastTabPos > 0){
            task_viewPager.setCurrentItem(lastTabPos,false);
            horizontalScrollView.post(() -> horizontalScrollView.scrollTo(storedPos, 0));
        }
    }

    private void syncTabWithFragments(){
        // to synchronize tabs with Viewpager we use tabLayoutMediator
        new TabLayoutMediator(tabLayout, task_viewPager, (tab, position) -> tab.setText(tabNames.get(position))).attach();
    }

    private void tabSelectedListener(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // to set the tab always on the visible screen
                int tabIndex = tab.getPosition();
                View tabView = Objects.requireNonNull(tabLayout.getTabAt(tabIndex)).view;
                int scrollX = tabView.getLeft() - (horizontalScrollView.getWidth() - tabView.getWidth()) / 2;
                horizontalScrollView.post(() -> {
                    horizontalScrollView.smoothScrollTo(scrollX, 0);
                    ceditor.putInt("tab", task_viewPager.getCurrentItem()).apply();
                    ceditor.putInt("scroll", horizontalScrollView.getScrollX()).apply();
                });

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void addNewFragment(){
        Dialog addListDialog = new Dialog(Dash_Board.this, R.style.NewTaskDialog);
        addListDialog.setContentView(R.layout.custom_add_list_layout);
        addListDialog.show();

        AppCompatButton saveTask;
        TextInputEditText listName;
        saveTask = addListDialog.findViewById(R.id.save_task);
        listName = addListDialog.findViewById(R.id.list_name);

        saveTask.setOnClickListener(v -> {

            if (Objects.requireNonNull(listName.getText()).toString().trim().isEmpty()) {
                // EditText field is empty, show error message
                listName.setError("Enter list name");
                // Optionally, you can request focus to the EditText field
                listName.requestFocus();
                // Return or show appropriate message to prevent further processing
                return;
            }
            ArrayList<Task_Model> arrTask = new ArrayList<>();
            ArrayList<Task_Model> arrTaskCompleted = new ArrayList<>();

            tabNames.add(Objects.requireNonNull(listName.getText()).toString().toUpperCase());
            databaseHelper.dailyTaskDao().addTask(new DailyTask("", "", '\0', "", false, listName.getText().toString().toUpperCase(), 1));

            // dynamically adding new fragments in the viewpager and select the new tab always
            ToDoFragment newFragment = new ToDoFragment(fragmentList.size(), arrTask, arrTaskCompleted, databaseHelper, tabNames.get(tabNames.size()-1), 1);
            fragmentList.add(newFragment);
            adapter.notifyItemInserted(fragmentList.size()-1);
            Objects.requireNonNull(tabLayout.getTabAt(fragmentList.size() - 1)).select();

            //use post method to first wait for layout change in the scrollview and then move
            // to the end of scrollview everytime a user adds a new tab
            horizontalScrollView.post(() -> horizontalScrollView.fullScroll((HorizontalScrollView.FOCUS_RIGHT)));

            addListDialog.dismiss();
        });

    }

    private void addNewTask(){

        deadline = "";
        deadlineTime = "";
        isDatechanged = false;

        Dialog addTaskDialog = new Dialog(Dash_Board.this, R.style.NewTaskDialog);
        addTaskDialog.setContentView(R.layout.custom_add_task_layout);
        addTaskDialog.show();

        TextInputEditText write_task, write_desc;
        AutoCompleteTextView select_priority;
        ImageView task_deadline, cancel_deadline;
        LinearLayout layout_show_deadline;
        TextView showDeadline;

        write_task = addTaskDialog.findViewById(R.id.write_task);
        write_desc = addTaskDialog.findViewById(R.id.write_desc);
        select_priority = addTaskDialog.findViewById(R.id.select_priority);
        task_deadline = addTaskDialog.findViewById(R.id.task_deadline);
        showDeadline = addTaskDialog.findViewById(R.id.show_deadline);
        layout_show_deadline = addTaskDialog.findViewById(R.id.layout_show_deadline);
        cancel_deadline = addTaskDialog.findViewById(R.id.cancel_deadline);

        Calendar calendar = Calendar.getInstance();

        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priority_select);
        select_priority.setAdapter(dropDownAdapter);

        cancel_deadline.setOnClickListener(v -> {
            deadline = "";
            layout_show_deadline.setVisibility(View.INVISIBLE);
            task_deadline.setVisibility(View.VISIBLE);
        });

        View.OnClickListener myClickListener = v -> {
            deadline = "";
            deadlineTime = "";
            isDatechanged = false;

            Dialog customDatePickerDialog = new Dialog(Dash_Board.this, R.style.CustomDatePickerDialog);
            customDatePickerDialog.setContentView(R.layout.custom_date_picker_dialog);
            customDatePickerDialog.show();

            TextView set_time;
            LinearLayout task_time;
            AppCompatButton select_date, cancel_date;

            set_time = customDatePickerDialog.findViewById(R.id.set_time);
            select_date = customDatePickerDialog.findViewById(R.id.select_date);
            cancel_date = customDatePickerDialog.findViewById(R.id.cancel_date);
            task_time = customDatePickerDialog.findViewById(R.id.task_time);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            task_time.setOnClickListener(v1 -> {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Dash_Board.this,
                        (view, hourOfDay, minute1) -> {
                            properTimeFormat(hourOfDay, minute1);
                            set_time.setText(deadlineTime);
                        }, hour, minute, false);

                timePickerDialog.show();
            });

            CalendarView datePicker = customDatePickerDialog.findViewById(R.id.datePicker);
            datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> properDateFormat(year, month, dayOfMonth));

            select_date.setOnClickListener(v12 -> {
                layout_show_deadline.setVisibility(View.VISIBLE);
                showDeadline.setText("");
                showDeadline.setVisibility(View.VISIBLE);
                task_deadline.setVisibility(View.INVISIBLE);

                if(isDatechanged){
                    deadline = deadline + " " + deadlineTime;
                    showDeadline.setText(shortenDeadline(deadline));

                }else{
                    Calendar calendar1 = Calendar.getInstance();
                    Date curr_date = calendar1.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    deadline = dateFormat.format(curr_date) + " " + deadlineTime;
                    showDeadline.setText(shortenDeadline(deadline));
                }
                customDatePickerDialog.dismiss();
            });

            cancel_date.setOnClickListener(v13 ->
                    customDatePickerDialog.dismiss());
            };

        task_deadline.setOnClickListener(myClickListener);

        showDeadline.setOnClickListener(myClickListener);

        AppCompatButton save_task;
        save_task = addTaskDialog.findViewById(R.id.save_task);

        save_task.setOnClickListener(v1 -> {
            String task, desc;
            char priority;
            int id;
            task = Objects.requireNonNull(write_task.getText()).toString();
            desc = Objects.requireNonNull(write_desc.getText()).toString();

            if(select_priority.getText().toString().equals("High")){
                priority = 'H';
            }else if(select_priority.getText().toString().equals("Medium")){
                priority = 'M';
            }else {
                priority = 'L';
            }

            task = task.trim();
            if (task.isEmpty()) {
                // EditText field is empty, show error message
                write_task.setError("Enter task");
                // Optionally, you can request focus to the EditText field
                write_task.requestFocus();
                // Return or show appropriate message to prevent further processing
                return;
            }

            // Proceed with further processing as the EditText field is not empty

            databaseHelper.dailyTaskDao().addTask(new DailyTask(task, desc, priority, deadline, false, getCurrTabName(), 0));

            id = databaseHelper.dailyTaskDao().getTaskId(task, desc, deadline, getCurrTabName());

            currFrag = fragmentList.get(task_viewPager.getCurrentItem());
            currFrag.addTask(id, task, desc, priority, deadline, getCurrTabName());
            emptyListImg(currFrag);

            if(!deadline.equals("")) {
                requestNotificationPermission();
                if(isNotificationAllowed = true)
                    setNotification(deadline, task, desc, id);
            }

            addTaskDialog.dismiss();

            if(currFrag.sortBy == 2)
                currFrag.sortByDate();

            if(currFrag.sortBy == 3)
                currFrag.sortByPriority();

            Handler handler = new Handler();

            // Define a runnable that will show the Toast after 1 second
            Runnable showToastRunnable = () -> Toast.makeText(Dash_Board.this, "Task added", Toast.LENGTH_SHORT).show();

            // Post the runnable with a delay of 1 second (1000 milliseconds)
            handler.postDelayed(showToastRunnable, 1000);

        });
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

    public void emptyListImg(ToDoFragment cfrag){

        if(cfrag.tasksPresent()){
            cfrag.no_tasks.setVisibility(View.INVISIBLE);
        }else{
            cfrag.no_tasks.setVisibility(View.VISIBLE);
        }
    }

    public String getCurrTabName(){
        String cTabName = null;
        int currentTabPosition = task_viewPager.getCurrentItem();
        TabLayout.Tab currentTab = tabLayout.getTabAt(currentTabPosition);
        if (currentTab != null) {
            cTabName = Objects.requireNonNull(currentTab.getText()).toString();
        }
        return cTabName;
    }

    private boolean isTabVisible(){
        boolean isVisible;
        int currentTabPosition = task_viewPager.getCurrentItem();
        View tabView = Objects.requireNonNull(tabLayout.getTabAt(currentTabPosition)).view;
        int tabLeft = tabView.getLeft();
        int tabRight = tabView.getRight();
        int scrollViewLeft = horizontalScrollView.getScrollX();
        int scrollViewRight = scrollViewLeft + horizontalScrollView.getWidth();
        isVisible = (tabLeft >= scrollViewLeft) && (tabRight <= scrollViewRight);
        return isVisible;

    }

    public void openTaskMenu(){

        LinearLayoutCompat rename_list, delete_list_layout, delete_completed_tasks;
        AppCompatTextView delete_list, delete_list_metadata, delete_completed_tasks_text;

        Dialog taskMenuDialog = new Dialog(Dash_Board.this, R.style.NewTaskDialog);
        taskMenuDialog.setContentView(R.layout.custom_task_menu_layout);

        Objects.requireNonNull(taskMenuDialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        taskMenuDialog.getWindow().setGravity(Gravity.BOTTOM);
        taskMenuDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        taskMenuDialog.show();

        rename_list = taskMenuDialog.findViewById(R.id.rename_list);
        delete_list = taskMenuDialog.findViewById(R.id.delete_list);
        delete_list_metadata = taskMenuDialog.findViewById(R.id.delete_list_metadata);
        delete_completed_tasks = taskMenuDialog.findViewById(R.id.delete_completed_tasks);
        delete_list_layout = taskMenuDialog.findViewById(R.id.delete_list_layout);
        delete_completed_tasks_text = taskMenuDialog.findViewById(R.id.delete_completed_tasks_text);

        Drawable bg = new ColorDrawable(Color.parseColor("#00FFFFFF"));

        if(task_viewPager.getCurrentItem() == 0){
            delete_list_metadata.setVisibility(View.VISIBLE);
            delete_list_layout.setBackground(bg);
            delete_list.setTextColor(Color.parseColor("#807777"));
            delete_list_metadata.setTextColor(Color.parseColor("#807777"));
        }else{
            delete_list_metadata.setVisibility(View.GONE);
        }

        if(fragmentList.get(task_viewPager.getCurrentItem()).arrTaskCompleted.isEmpty()){
            delete_completed_tasks.setBackground(bg);
            delete_completed_tasks_text.setTextColor(Color.parseColor("#807777"));
        }

        rename_list.setOnClickListener(v -> {
            taskMenuDialog.dismiss();
            renameList();
        });

        delete_list_layout.setOnClickListener(v -> {
            if(task_viewPager.getCurrentItem() != 0){
                taskMenuDialog.dismiss();
            }
            deleteList();
        });

        delete_completed_tasks.setOnClickListener(v ->{
            if(!fragmentList.get(task_viewPager.getCurrentItem()).arrTaskCompleted.isEmpty()){
                taskMenuDialog.dismiss();
                deleteCompletedTasks();
            }
        });

    }

    public void renameList(){
        Dialog renameListDialog = new Dialog(Dash_Board.this, R.style.NewTaskDialog);
        renameListDialog.setContentView(R.layout.custom_add_list_layout);
        renameListDialog.show();

        AppCompatButton renameList;
        TextInputEditText listName;
        renameList = renameListDialog.findViewById(R.id.save_task);
        renameList.setText("Rename");
        listName = renameListDialog.findViewById(R.id.list_name);
        int currentTabPosition = task_viewPager.getCurrentItem();
        TabLayout.Tab currentTab = tabLayout.getTabAt(currentTabPosition);
        String oldName = getCurrTabName();
        listName.setText(oldName);

        renameList.setOnClickListener(v1 -> {
            if (Objects.requireNonNull(listName.getText()).toString().trim().isEmpty()) {
                // EditText field is empty, show error message
                listName.setError("Enter list name");
                // Optionally, you can request focus to the EditText field
                listName.requestFocus();
                // Return or show appropriate message to prevent further processing
                return;
            }

            tabNames.set(currentTabPosition, listName.getText().toString().toUpperCase());
            fragmentList.get(task_viewPager.getCurrentItem()).listName = listName.getText().toString().toUpperCase();
            Objects.requireNonNull(currentTab).setText(listName.getText().toString().toUpperCase());
            databaseHelper.dailyTaskDao().renameList(listName.getText().toString().toUpperCase(), oldName);
            renameListDialog.dismiss();
        });
    }

    public void deleteList(){
        int pos = task_viewPager.getCurrentItem();
        if(pos == 0)
            return;
        fragmentList.remove(pos);
        adapter.notifyItemRemoved(pos);
        Objects.requireNonNull(tabLayout.getTabAt(pos - 1)).select();
        databaseHelper.dailyTaskDao().deleteList(tabNames.get(pos).toUpperCase());
        tabNames.remove(pos);
    }

    public void deleteCompletedTasks(){
        currFrag = fragmentList.get(task_viewPager.getCurrentItem());
        currFrag.arrTaskCompleted.clear();
        currFrag.adapter.completed_tasks_list.setVisibility(View.GONE);
        currFrag.adapter.seperation_line.setVisibility(View.GONE);
        currFrag.adapter.adapter.notifyDataSetChanged();

        if(currFrag.arrTask.isEmpty()){
            currFrag.no_tasks.setVisibility(View.VISIBLE);
        }

        databaseHelper.dailyTaskDao().deleteCompletedTasks(tabNames.get(task_viewPager.getCurrentItem()).toUpperCase());
    }

    public void sortTasks() {
        Dialog taskMenuDialog = new Dialog(Dash_Board.this, R.style.NewTaskDialog);
        taskMenuDialog.setContentView(R.layout.custom_task_menu_layout);

        Objects.requireNonNull(taskMenuDialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        taskMenuDialog.getWindow().setGravity(Gravity.BOTTOM);
        taskMenuDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        taskMenuDialog.show();

        LinearLayoutCompat my_order_layout, date_layout, priority_layout, heading;
        AppCompatTextView date_text, priority_text, my_order_text;
        ImageView tick1, tick2, tick3;

        heading = taskMenuDialog.findViewById(R.id.heading);
        my_order_layout = taskMenuDialog.findViewById(R.id.rename_list);
        my_order_text = taskMenuDialog.findViewById(R.id.rename_list_text);
        tick1 = taskMenuDialog.findViewById(R.id.tick1);
        date_layout = taskMenuDialog.findViewById(R.id.delete_list_layout);
        date_text = taskMenuDialog.findViewById(R.id.delete_list);
        tick2 = taskMenuDialog.findViewById(R.id.tick2);
        priority_layout = taskMenuDialog.findViewById(R.id.delete_completed_tasks);
        priority_text = taskMenuDialog.findViewById(R.id.delete_completed_tasks_text);
        tick3 = taskMenuDialog.findViewById(R.id.tick3);

        heading.setVisibility(View.VISIBLE);
        my_order_text.setText("My Order");
        date_text.setText("Date");
        priority_text.setText("Priority");

        ToDoFragment frag = fragmentList.get(task_viewPager.getCurrentItem());

        if(frag.sortBy==1) {
            tick1.setVisibility(View.VISIBLE);
            tick2.setVisibility(View.INVISIBLE);
            tick3.setVisibility(View.INVISIBLE);
        }
        else if (frag.sortBy==2) {
            tick2.setVisibility(View.VISIBLE);
            tick1.setVisibility(View.INVISIBLE);
            tick3.setVisibility(View.INVISIBLE);
        }else {
            tick3.setVisibility(View.VISIBLE);
            tick2.setVisibility(View.INVISIBLE);
            tick1.setVisibility(View.INVISIBLE);
        }

        date_layout.setOnClickListener(v -> {
            tick2.setVisibility(View.VISIBLE);
            tick1.setVisibility(View.INVISIBLE);
            tick3.setVisibility(View.INVISIBLE);
            frag.sortByDate();
            taskMenuDialog.dismiss();
        });

        my_order_layout.setOnClickListener(v -> {
            tick2.setVisibility(View.INVISIBLE);
            tick1.setVisibility(View.VISIBLE);
            tick3.setVisibility(View.INVISIBLE);
            frag.sortByMyOrder();
            taskMenuDialog.dismiss();
        });

        priority_layout.setOnClickListener(v -> {
            tick2.setVisibility(View.INVISIBLE);
            tick1.setVisibility(View.INVISIBLE);
            tick3.setVisibility(View.VISIBLE);
            frag.sortByPriority();
            taskMenuDialog.dismiss();
        });

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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "deadline_notification",
                    "Deadline Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setNotification(String deadlineString, String task_name, String task_des, int task_id) {

            if (parseDate(deadlineString) == null || isCurrentDateGreaterThan(parseDate(deadlineString))) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Objects.requireNonNull(parseDate(deadlineString)));

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("task_name", task_name);
            intent.putExtra("task_description", task_des);
            intent.putExtra("notification_id", task_id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task_id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

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
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                isNotificationAllowed = true;    // Permission is already granted, proceed with notifications
                createNotificationChannel();
            }
        } else {
                isNotificationAllowed = true;    // No need to request permission, proceed with notifications
                createNotificationChannel();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createNotificationChannel();    // Permission was granted, proceed with notifications
                isNotificationAllowed = true;
            } else {
                isNotificationAllowed = false;     // Permission was denied, show an appropriate message to the user
            }
        }
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

    private boolean isCurrentDateGreaterThan(Date givenDate) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Compare the dates
        return currentDate.after(givenDate);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currFragPos", task_viewPager.getCurrentItem());
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences cpref = getSharedPreferences("IdentifyCurrTab", MODE_PRIVATE);
        SharedPreferences.Editor editor = cpref.edit();
        if(isTabVisible()) {
            editor.putInt("tab", task_viewPager.getCurrentItem()).apply();
            editor.putInt("scroll", horizontalScrollView.getScrollX()).apply();
        }

    }

}