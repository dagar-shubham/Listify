    package com.example.myapplication.Adapters;

    import android.animation.Animator;
    import android.annotation.SuppressLint;
    import android.app.AlarmManager;
    import android.app.PendingIntent;
    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Color;
    import android.os.Handler;
    import android.os.Looper;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.widget.AppCompatTextView;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.airbnb.lottie.LottieAnimationView;
    import com.example.myapplication.Activities.Dash_Board;
    import com.example.myapplication.Activities.UpdateDeleteTask;
    import com.example.myapplication.Database.DailyTask;
    import com.example.myapplication.Fragments.ToDoFragment;
    import com.example.myapplication.R;
    import com.example.myapplication.Utilities.NotificationReceiver;
    import com.example.myapplication.Models.Task_Model;

    import java.util.Calendar;
    import java.util.Collections;
    import java.util.Date;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    public class ToDoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_NOT_COMPLETED = 0;
        private static final int VIEW_TYPE_COMPLETED = 1;

        Context context;
        boolean isClickable = true;
        boolean isCompletedTaskListVisible = false;
        ToDoFragment fragment;
        public ToDoNestedRecyclerAdapter adapter;
        RecyclerView expandable_list;
        public LinearLayout completed_tasks_list;
        public View seperation_line;

        public ToDoRecyclerAdapter(ToDoFragment fragment) {
            this.fragment = fragment;
        }

        public int getItemViewType(int position) {
            return (position == getItemCount() - 1) ? VIEW_TYPE_COMPLETED : VIEW_TYPE_NOT_COMPLETED;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            RecyclerView.ViewHolder viewHolder;
            context = parent.getContext();
          //  databaseHelper = DatabaseHelper.getDB(parent.getContext());
            if (viewType == VIEW_TYPE_COMPLETED) {
                View view = inflater.inflate(R.layout.to_do_list_nested_layout, parent, false);
                viewHolder = new ViewHolderCompleted(view);

            } else {
                View view = inflater.inflate(R.layout.to_do_list_layout, parent, false);
                viewHolder = new ViewHolderNotCompleted(view);
            }
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            if (getItemViewType(position) == VIEW_TYPE_NOT_COMPLETED) {
                String task_n, task_d, task_dline;
                char task_p;
                task_n = fragment.arrTask.get(position).task;
                task_d = fragment.arrTask.get(position).task_desc;
                task_p = fragment.arrTask.get(position).task_priority;
                task_dline = fragment.arrTask.get(position).task_deadline;

                ((ViewHolderNotCompleted)holder).setData(task_n, task_d, task_dline, task_p);

                ((ViewHolderNotCompleted)holder).task_done.setOnClickListener(v -> {
                    if(isClickable)
                        ((ViewHolderNotCompleted)holder).taskDone(holder.getAdapterPosition());
                });

                ((ViewHolderNotCompleted)holder).task_name.setOnClickListener(v -> ((ViewHolderNotCompleted)holder).updateDeleteTask(holder.getAdapterPosition()));
                ((ViewHolderNotCompleted)holder).task_des.setOnClickListener(v -> ((ViewHolderNotCompleted)holder).updateDeleteTask(holder.getAdapterPosition()));
                ((ViewHolderNotCompleted)holder).task_priority.setOnClickListener(v -> ((ViewHolderNotCompleted)holder).updateDeleteTask(holder.getAdapterPosition()));
                ((ViewHolderNotCompleted)holder).task_date_time.setOnClickListener(v -> ((ViewHolderNotCompleted)holder).updateDeleteTask(holder.getAdapterPosition()));

            }
            else {
                // Bind data for nested RecyclerView item (e.g., set nested recycler \View adapter and listeners)
                ((ViewHolderCompleted)holder).setData();

                completed_tasks_list.setOnClickListener(v -> ((ViewHolderCompleted)holder).dropDown());
            }

        }

        @Override
        public int getItemCount() {
            // Add 1 for the nested recyclerView item
            return fragment.arrTask.size()+1;
        }

        public void swapItems(int fromPosition, int toPosition) {
            int temp;
            temp = fragment.arrTask.get(fromPosition).getTask_id();
            fragment.arrTask.get(fromPosition).setTask_id(fragment.arrTask.get(toPosition).getTask_id());
            fragment.arrTask.get(toPosition).setTask_id(temp);
            // Swap the items in your data list
            Collections.swap(fragment.arrTask, fromPosition, toPosition);
            // Notify the adapter that the data has changed
            notifyItemMoved(fromPosition, toPosition);

            DailyTask task1 = new DailyTask(fragment.arrTask.get(fromPosition));
            DailyTask task2 = new DailyTask(fragment.arrTask.get(toPosition));

            fragment.databaseHelper.dailyTaskDao().updateTask(task1);
            fragment.databaseHelper.dailyTaskDao().updateTask(task2);
        }


        public class ViewHolderNotCompleted extends RecyclerView.ViewHolder {
            AppCompatTextView task_name, task_priority, task_des, task_date_time;
            ImageView task_done;
            LottieAnimationView task_done_anim;
            boolean isTaskMarkedCompleted;

            public ViewHolderNotCompleted(@NonNull View itemView) {
                super(itemView);

                task_name = itemView.findViewById(R.id.task_name);
                task_des = itemView.findViewById(R.id.task_des);
                task_priority = itemView.findViewById(R.id.task_priority);
                task_date_time = itemView.findViewById(R.id.task_date_time);
                task_done = itemView.findViewById(R.id.task_done);
                task_done_anim = itemView.findViewById(R.id.task_done_anim);
                isTaskMarkedCompleted = false;

            }

            public void setData(String task_n, String task_d, String task_dline, Character task_p) {
                task_name.setText(task_n);
                task_des.setText(task_d);
                task_priority.setText(Character.toString(task_p));
                task_date_time.setText(shortenDeadline(task_dline));

                if (task_d.equals("")) {
                    task_des.setVisibility(View.GONE);
                } else {
                    task_des.setVisibility(View.VISIBLE);
                }

                if (task_dline.equals("")) {
                    task_date_time.setVisibility(View.GONE);
                } else {
                    task_date_time.setVisibility(View.VISIBLE);
                    if(isCurrentDateGreaterThan(Dash_Board.parseDate(task_dline)))
                        task_date_time.setTextColor(Color.parseColor("#D11818"));

                }

                if (task_p == 'H') {
                    task_priority.setTextColor(Color.parseColor("#F43F3F"));
                } else if (task_p == 'M') {
                    task_priority.setTextColor(Color.parseColor("#3FC7F4"));
                } else {
                    task_priority.setTextColor(Color.parseColor("#879393"));
                }
            }

            public void taskDone(int pos) {

                isClickable = false;

                ExecutorService service = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                service.execute(() -> {
                    fragment.databaseHelper.dailyTaskDao().updateTaskStatus(true, fragment.arrTask.get(pos).getTask_id());

                    handler.post(() -> {

                        task_done.setVisibility(View.INVISIBLE);
                        task_done_anim.setVisibility(View.VISIBLE);

                        task_done_anim.playAnimation();

                        task_done_anim.addAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(@NonNull Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(@NonNull Animator animation) {
                                if (fragment.arrTask.isEmpty() || pos >= fragment.arrTask.size()) {
                                    return;
                                }

                                //to handle double tap while completing a task to avoid abnormalities
                                if (isTaskMarkedCompleted) {
                                    isTaskMarkedCompleted = false;
                                    return;
                                }
                                isTaskMarkedCompleted = true;

                                // Update UI elements here (e.g., view visibility changes)
                                task_done_anim.setVisibility(View.GONE);
                                task_done.setVisibility(View.VISIBLE);
                                fragment.arrTask.get(pos).setTask_completed(true);
                                fragment.arrTaskCompleted.add(fragment.arrTask.get(pos));
                                cancelNotification(fragment.arrTask.get(pos));
                                if(adapter!=null)
                                    adapter.notifyItemInserted(fragment.arrTaskCompleted.size() - 1);

                                fragment.arrTask.remove(pos);
                                notifyItemRemoved(pos);

                                if(isCompletedTaskListVisible) {
                                    if (fragment.arrTask.isEmpty())
                                        seperation_line.setVisibility(View.GONE);
                                    else
                                        seperation_line.setVisibility(View.VISIBLE);

                                    if (fragment.arrTaskCompleted.size() != 0) {
                                        completed_tasks_list.setVisibility(View.VISIBLE);
                                    }
                                }

                                if(fragment.sortBy == 1)
                                    fragment.sortByMyOrder();
                                else if(fragment.sortBy == 2)
                                    fragment.sortByDate();
                                else
                                    fragment.sortByPriority();

                                Toast.makeText(task_done.getContext(), "task completed", Toast.LENGTH_SHORT).show();
                                isClickable = true;

                            }

                            @Override
                            public void onAnimationCancel(@NonNull Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(@NonNull Animator animation) {

                            }
                        });

                    });
                });

            }

            public void updateDeleteTask(int pos){
                Intent intent = new Intent(itemView.getContext(), UpdateDeleteTask.class);
                intent.putExtra("task_id", fragment.arrTask.get(pos).getTask_id());
                intent.putExtra("task_name", fragment.arrTask.get(pos).getTaskName());
                intent.putExtra("task_des", fragment.arrTask.get(pos).getTask_desc());
                intent.putExtra("task_priority", fragment.arrTask.get(pos).getTask_priority());
                intent.putExtra("task_deadline", fragment.arrTask.get(pos).getTask_deadline());
                intent.putExtra("isTaskCompleted", fragment.arrTask.get(pos).getTask_completed());
                intent.putExtra("list_name", fragment.arrTask.get(pos).getList_name());
                itemView.getContext().startActivity(intent);

            }
        }

        public class ViewHolderCompleted extends RecyclerView.ViewHolder {
            ImageButton drop_down_btn;
            Boolean isExpanded;
            public ViewHolderCompleted(View itemView) {
                super(itemView);
                isExpanded = false;
                completed_tasks_list = itemView.findViewById(R.id.completed_tasks_list);
                expandable_list = itemView.findViewById(R.id.expandable_list);
                drop_down_btn = itemView.findViewById(R.id.drop_down_btn);
                seperation_line = itemView.findViewById(R.id.seperation_line);
            }

            public void setData(){
                isCompletedTaskListVisible = true;
                if(fragment.arrTaskCompleted.isEmpty())
                    seperation_line.setVisibility(View.GONE);

                adapter = new ToDoNestedRecyclerAdapter(fragment);
                expandable_list.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                expandable_list.setAdapter(adapter);
                showCompletedTaskList();
            }

            public void showCompletedTaskList(){
                if(fragment.arrTaskCompleted.size() != 0){
                    completed_tasks_list.setVisibility(View.VISIBLE);
                }else{
                    completed_tasks_list.setVisibility(View.GONE);
                }
            }

            public void dropDown(){
                if(isExpanded) {
                    expandable_list.setVisibility(View.GONE);
                    drop_down_btn.setBackgroundResource(R.drawable.drop_down);
                    isExpanded = false;
                }else{
                    expandable_list.setVisibility(View.VISIBLE);
                    drop_down_btn.setBackgroundResource(R.drawable.drop_up);
                    isExpanded = true;
                }
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

        private void cancelNotification(Task_Model task){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getTask_id(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
            if (pendingIntent != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);  // Cancel the alarm
                pendingIntent.cancel();  // Cancel the PendingIntent
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
