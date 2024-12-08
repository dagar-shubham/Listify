package com.example.myapplication.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.myapplication.Models.Task_Model;


@Entity(tableName = "to_do_tasks")
public class DailyTask {

    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "task_name")
    private String taskName;

    @ColumnInfo(name = "task_desc")
    private String taskDesc;

    @ColumnInfo(name = "task_priority")
    private char taskPriority;

    @ColumnInfo(name = "task_date_time")
    private String taskDateTime;

    @ColumnInfo(name = "task_completed")
    private Boolean taskCompleted;

    @ColumnInfo(name = "list_name")
    private String listName;

    @ColumnInfo(name = "sort_by")
    private int sortBy;

    public DailyTask(int id, String taskName, String taskDesc, char taskPriority, String taskDateTime, Boolean taskCompleted, String listName, int sortBy) {
        this.id = id;
        this.taskName = taskName;
        this.taskDesc = taskDesc;
        this.taskPriority = taskPriority;
        this.taskDateTime = taskDateTime;
        this.taskCompleted = taskCompleted;
        this.listName = listName;
        this.sortBy = sortBy;
    }

    @Ignore
    public DailyTask(String taskName, String taskDesc, char taskPriority, String taskDateTime, Boolean taskCompleted, String listName, int sortBy) {
        this.taskName = taskName;
        this.taskDesc = taskDesc;
        this.taskPriority = taskPriority;
        this.taskDateTime = taskDateTime;
        this.taskCompleted = taskCompleted;
        this.listName = listName;
        this.sortBy = sortBy;
    }

    public DailyTask(Task_Model task){
        this.id = task.getTask_id();
        this.taskName = task.getTaskName();
        this.taskDesc = task.getTask_desc();
        this.taskPriority = task.getTask_priority();
        this.taskDateTime = task.getTask_deadline();
        this.taskCompleted = task.getTask_completed();
        this.listName = task.getList_name();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public char getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(char taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getTaskDateTime() {
        return taskDateTime;
    }

    public void setTaskDateTime(String taskDateTime) {
        this.taskDateTime = taskDateTime;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public Boolean getTaskCompleted() {
        return taskCompleted;
    }

    public void setTaskCompleted(Boolean taskCompleted) {
        this.taskCompleted = taskCompleted;
    }

    public int getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }
}
