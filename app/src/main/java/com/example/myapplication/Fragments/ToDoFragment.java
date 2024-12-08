package com.example.myapplication.Fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.example.myapplication.Database.DailyTask;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Comparator.DateTimeComparator;
import com.example.myapplication.Models.Task_Model;
import com.example.myapplication.Comparator.PriorityComparator;
import com.example.myapplication.R;
import com.example.myapplication.Adapters.ToDoRecyclerAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class ToDoFragment extends Fragment {

    public int sortBy;
    ConstraintLayout frameLayout4;
    int id;
    public String listName;
    Context context;
    public LinearLayout no_tasks;
    RecyclerView tasks_list;
    public ToDoRecyclerAdapter adapter;
    public ArrayList<Task_Model> arrTask;
    public ArrayList<Task_Model> arrTaskCompleted;
    public DatabaseHelper databaseHelper;

    public ToDoFragment(){

    }

    public ToDoFragment(int id, ArrayList<Task_Model> arrTask, ArrayList<Task_Model> arrTaskCompleted, DatabaseHelper databaseHelper,String listName, int sortBy){
        this.id = id;
        this.arrTask = arrTask;
        this.arrTaskCompleted = arrTaskCompleted;
        this.databaseHelper = databaseHelper;
        this.listName = listName;
        this.sortBy = sortBy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_to_do, container, false);

        tasks_list = rootView.findViewById(R.id.tasks_list);        // "findViewById()" method should be called on the inflated view of the fragment's layout

        no_tasks = rootView.findViewById(R.id.no_tasks);

        context = getContext();

        frameLayout4 = rootView.findViewById(R.id.frameLayout4);

        tasks_list.setLayoutManager(new LinearLayoutManager(context));

        if (savedInstanceState != null) {
            arrTask = (ArrayList<Task_Model>) savedInstanceState.getSerializable("tasks");
            arrTaskCompleted = (ArrayList<Task_Model>) savedInstanceState.getSerializable("tasksCompleted");
            id = savedInstanceState.getInt("fragment_id");
            databaseHelper = DatabaseHelper.getDB(context);
            sortBy = savedInstanceState.getInt("sortBy");
            listName = savedInstanceState.getString("listName");
        }

        adapter = new ToDoRecyclerAdapter(ToDoFragment.this);
        tasks_list.setAdapter(adapter);

        if(tasksPresent()){
            no_tasks.setVisibility(View.INVISIBLE);
        }else{
            no_tasks.setVisibility(View.VISIBLE);
        }

        if(sortBy==2)
            sortByDate();

        if(sortBy == 3)
            sortByPriority();

        //to implement drag and drop feature
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // Implement reordering logic here

                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                if(fromPosition >= arrTask.size() || toPosition >= arrTask.size() || fromPosition < 0 || toPosition < 0){
                    return false;
                }

                // Swap items in your data list
                adapter.swapItems(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Implement swipe action (if needed)
            }


          //   to allow drag and drop for all items in the recyclerview except the last item (which is the nested recyclerview)
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if(sortBy != 1) {
                    Snackbar.make(frameLayout4, "To move a task in a list, select sortBy My Order", Snackbar.LENGTH_SHORT).show();
                    return 0;
                }
                int itemCount = arrTask.size();
                if (position == itemCount) {
                    return 0;
                }

                // Otherwise, enable drag (up and down)
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    // Item is being dragged, change its background color
                    if (viewHolder != null) {
                        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                        switch (currentNightMode) {
                            case Configuration.UI_MODE_NIGHT_NO:
                                // Light mode
                                viewHolder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_item_bg_light));
                                break;
                            case Configuration.UI_MODE_NIGHT_YES:
                                // Dark mode
                                viewHolder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_item_bg_dark));
                                break;
                            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                                // Undefined or system default mode
                                break;
                        }
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Dragging is complete, revert the background color to transparent
                viewHolder.itemView.setBackground(null);
            }

        });

        itemTouchHelper.attachToRecyclerView(tasks_list);

        return rootView;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tasks", arrTask);
        outState.putSerializable("tasksCompleted", arrTaskCompleted);
        outState.putInt("fragment_id", id);
        outState.putInt("sortBy", sortBy);
        outState.putString("listName", listName);
    }

    public void addTask(int id, String task_name, String task_desc, char priority, String task_deadline, String task_list){
        int pos = 0;

        arrTask.add(new Task_Model(id, task_name, task_desc, priority, task_deadline, false, task_list));

        if(adapter != null) {
            adapter.notifyItemInserted(arrTask.size() - 1);
            if (!arrTaskCompleted.isEmpty())
                adapter.seperation_line.setVisibility(View.VISIBLE);

            for(int i = 0; i<arrTask.size(); i++){
                if(arrTask.get(i).getTask_id() == id)
                    pos = i;
            }
            Handler handler = new Handler();
            int finalPos = pos;
            handler.postDelayed(() -> tasks_list.smoothScrollToPosition(finalPos), 500);

        }

    }

    public int getFragId(){
        return id;
    }

    public void sortByDate(){
        sortBy = 2;
        DateTimeComparator comparator = new DateTimeComparator();
        arrTask.sort(comparator);
        adapter.notifyDataSetChanged();
    }

    public void sortByMyOrder(){
        sortBy = 1;
        arrTask.clear();
        ArrayList<DailyTask> arrDailyTask = (ArrayList<DailyTask>) databaseHelper.dailyTaskDao().getTasksOfList(listName, '\0');

        for(int j=0; j<arrDailyTask.size(); j++){
            Task_Model temp = new Task_Model(arrDailyTask.get(j).getId(), arrDailyTask.get(j).getTaskName(), arrDailyTask.get(j).getTaskDesc(), arrDailyTask.get(j).getTaskPriority(), arrDailyTask.get(j).getTaskDateTime(), arrDailyTask.get(j).getTaskCompleted(), arrDailyTask.get(j).getListName());
            if (!arrDailyTask.get(j).getTaskCompleted())
                arrTask.add(temp);

        }
        adapter.notifyDataSetChanged();

    }

    public void sortByPriority(){
        sortBy = 3;
        PriorityComparator comparator = new PriorityComparator();
        arrTask.sort(comparator);
        adapter.notifyDataSetChanged();
    }

    public boolean tasksPresent(){
        return arrTask.size() > 0 || arrTaskCompleted.size() >0;
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseHelper.dailyTaskDao().updateSortByStatus(sortBy, listName, '\0');
    }
}