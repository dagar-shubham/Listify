package com.example.myapplication.Adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.UpdateDeleteTask;
import com.example.myapplication.Fragments.ToDoFragment;
import com.example.myapplication.R;
import com.example.myapplication.Utilities.NotificationReceiver;
import com.example.myapplication.Models.Task_Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToDoNestedRecyclerAdapter extends RecyclerView.Adapter<ToDoNestedRecyclerAdapter.ViewHolder> {
    ToDoFragment fragment;
    Context context;

    public ToDoNestedRecyclerAdapter(ToDoFragment fragment){
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        ViewHolder viewHolder;
        View view = inflater.inflate(R.layout.to_do_list_layout, parent, false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(fragment.arrTaskCompleted.size() != 0) {
            String task_n, task_d, task_dline;
            char task_p;
            task_n = fragment.arrTaskCompleted.get(position).task;
            task_d = fragment.arrTaskCompleted.get(position).task_desc;
            task_p = fragment.arrTaskCompleted.get(position).task_priority;
            task_dline = fragment.arrTaskCompleted.get(position).task_deadline;

            holder.setData(task_n, task_d, task_dline, task_p);

            holder.task_did.setOnClickListener(v -> holder.taskUnDone(holder.getAdapterPosition()));

            holder.task_name.setOnClickListener(v -> holder.taskUpdateDelete(holder.getAdapterPosition()));
            holder.task_des.setOnClickListener(v -> holder.taskUpdateDelete(holder.getAdapterPosition()));
            holder.task_priority.setOnClickListener(v -> holder.taskUpdateDelete(holder.getAdapterPosition()));
            holder.task_date_time.setOnClickListener(v -> holder.taskUpdateDelete(holder.getAdapterPosition()));

        }
    }



    @Override
    public int getItemCount() {
        return fragment.arrTaskCompleted.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        AppCompatTextView task_name, task_priority, task_des, task_date_time;
        View task_done_line;
        ImageView task_done, task_did;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            task_name = itemView.findViewById(R.id.task_name);
            task_des = itemView.findViewById(R.id.task_des);
            task_priority = itemView.findViewById(R.id.task_priority);
            task_date_time = itemView.findViewById(R.id.task_date_time);
            task_done = itemView.findViewById(R.id.task_done);
            task_done_line = itemView.findViewById(R.id.task_done_line);
            task_did = itemView.findViewById(R.id.task_did);
        }

        public void setData(String task_n, String task_d, String task_dline, Character task_p){
            task_name.setText(task_n);
            task_des.setText(task_d);
            task_priority.setText(Character.toString(task_p));
            task_date_time.setText(shortenDeadline(task_dline));

            task_done.setVisibility(View.INVISIBLE);
            task_did.setVisibility(View.VISIBLE);
            task_done_line.setVisibility(View.VISIBLE);

            if(task_d.equals("")){
                task_des.setVisibility(View.GONE);
            }else{
                task_des.setVisibility(View.VISIBLE);
            }

            if(task_dline.equals("")){
                task_date_time.setVisibility(View.GONE);
            }else{
                task_date_time.setVisibility(View.VISIBLE);
            }

            if(task_p == 'H'){
                task_priority.setTextColor(Color.parseColor("#F43F3F"));
            } else if(task_p == 'M'){
                task_priority.setTextColor(Color.parseColor("#3FC7F4"));
            } else {
                task_priority.setTextColor(Color.parseColor("#879393"));
            }
        }

        public void taskUnDone(int pos){

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                fragment.databaseHelper.dailyTaskDao().updateTaskStatus(false, fragment.arrTaskCompleted.get(pos).getTask_id());


                handler.post(() -> {
                    task_did.setVisibility(View.INVISIBLE);

                    if (fragment.arrTaskCompleted.isEmpty() || pos >= fragment.arrTaskCompleted.size() ) {
                        return;
                    }

                    // Update UI elements here (e.g., view visibility changes)
                    task_done.setVisibility(View.VISIBLE);
                    fragment.arrTaskCompleted.get(pos).setTask_completed(false);
                    int prevIndex = fragment.databaseHelper.dailyTaskDao().identifyPos(fragment.arrTaskCompleted.get(pos).getTask_id(), '\0', false, fragment.arrTaskCompleted.get(pos).getList_name());
                    fragment.arrTask.add(prevIndex, fragment.arrTaskCompleted.get(pos));
                    fragment.adapter.notifyItemInserted(prevIndex);

                    updateNotification(fragment.arrTaskCompleted.get(pos));

                    if(fragment.sortBy == 1)
                        fragment.sortByMyOrder();
                    else if(fragment.sortBy == 2)
                        fragment.sortByDate();
                    else
                        fragment.sortByPriority();

                    fragment.arrTaskCompleted.remove(pos);
                    notifyItemRemoved(pos);

                    if(fragment.arrTask.isEmpty())
                        fragment.adapter.seperation_line.setVisibility(View.GONE);
                    else
                        fragment.adapter.seperation_line.setVisibility(View.VISIBLE);

                    if (fragment.arrTaskCompleted.isEmpty()) {
                        fragment.adapter.completed_tasks_list.setVisibility(View.GONE);
                        fragment.adapter.seperation_line.setVisibility(View.GONE);
                    }

                    Toast.makeText(task_done.getContext(), "task undone", Toast.LENGTH_SHORT).show();

                });

            });

            }

        public void taskUpdateDelete(int pos) {
            Intent intent = new Intent(itemView.getContext(), UpdateDeleteTask.class);
            intent.putExtra("task_id", fragment.arrTaskCompleted.get(pos).getTask_id());
            intent.putExtra("task_name", fragment.arrTaskCompleted.get(pos).getTaskName());
            intent.putExtra("task_des", fragment.arrTaskCompleted.get(pos).getTask_desc());
            intent.putExtra("task_priority", fragment.arrTaskCompleted.get(pos).getTask_priority());
            intent.putExtra("task_deadline", fragment.arrTaskCompleted.get(pos).getTask_deadline());
            intent.putExtra("isTaskCompleted", fragment.arrTaskCompleted.get(pos).getTask_completed());
            intent.putExtra("list_name", fragment.arrTaskCompleted.get(pos).getList_name());
            itemView.getContext().startActivity(intent);
        }
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

    private void updateNotification(Task_Model task){

        // Set a new newNotification with updated data
        if (task.getTask_deadline().equals("") || task.getTask_completed() || isCurrentDateGreaterThan(parseDate(task.getTask_deadline()))) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(parseDate(task.getTask_deadline())));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_name", task.getTaskName());
        intent.putExtra("task_description", task.getTask_desc());
        intent.putExtra("notification_id", task.getTask_id());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getTask_id(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

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
}
