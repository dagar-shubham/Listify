package com.example.myapplication.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DailyTaskDao{

    @Query("select distinct list_name from to_do_tasks")
    List<String> getAllLists();

    @Query("select * from to_do_tasks")
    List<DailyTask> getAllTasks();

    @Insert
    void addTask(DailyTask dailyTask);

    @Update
    void updateTask(DailyTask dailyTask);

    @Query("UPDATE to_do_tasks SET task_completed = :status WHERE id = :task_id")
    void updateTaskStatus(boolean status, int task_id);

    @Query("SELECT id FROM to_do_tasks WHERE task_name = :task AND task_desc = :dec AND task_date_time = :time AND list_name = :list")
    int getTaskId(String task, String dec, String time, String list);

    @Query("SELECT COUNT(*) FROM to_do_tasks WHERE :taskId > id AND task_priority != :priority AND task_completed = :isCompleted AND list_name = :list")
    int identifyPos(int taskId, char priority, boolean isCompleted, String list);

    @Query("UPDATE to_do_tasks SET list_name = :updatedList WHERE list_name = :oldName")
    void renameList(String updatedList, String oldName);

    @Query("DELETE FROM to_do_tasks WHERE list_name = :list")
    void deleteList(String list);

    @Query("DELETE FROM to_do_tasks WHERE list_name = :list AND task_completed = true")
    void deleteCompletedTasks(String list);

    @Query("SELECT sort_by FROM to_do_tasks WHERE list_name = :list AND task_priority = :priority")
    int getSortBy(String list, char priority);

    @Query("UPDATE to_do_tasks SET sort_by = :sort WHERE list_name = :list AND task_priority = :priority")
    void updateSortByStatus(int sort, String list, char priority);

    @Query("SELECT * FROM to_do_tasks WHERE list_name = :list AND task_priority != :priority")
    List<DailyTask> getTasksOfList(String list, char priority);

    @Query("DELETE FROM to_do_tasks WHERE id = :task_id")
    void deleteTask(int task_id);
}
