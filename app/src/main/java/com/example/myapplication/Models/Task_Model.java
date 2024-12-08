package com.example.myapplication.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Task_Model implements Serializable {

    public int task_id;
    public String task;
    public String task_desc;
    public String task_deadline;
    public char task_priority;
    public Boolean task_completed;
    public String list_name;

    public Task_Model(int task_id, String task, String task_desc, char task_priority, String task_deadline, Boolean task_completed, String list_name) {

        this.task_id = task_id;
        this.task = task;
        this.task_desc = task_desc;
        this.task_priority = task_priority;
        this.task_deadline = task_deadline;
        this.task_completed = task_completed;
        this.list_name = list_name;
    }


    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getTaskName() {
        return task;
    }

    public void setTaskName(String task) {
        this.task = task;
    }

    public String getTask_desc() {
        return task_desc;
    }

    public void setTask_desc(String task_desc) {
        this.task_desc = task_desc;
    }

    public String getTask_deadline() {
        return task_deadline;
    }

    public void setTask_deadline(String task_deadline) {
        this.task_deadline = task_deadline;
    }

    public char getTask_priority() {
        return task_priority;
    }

    public void setTask_priority(char task_priority) {
        this.task_priority = task_priority;
    }
    public Boolean getTask_completed() {
        return task_completed;
    }

    public void setTask_completed(Boolean task_completed) {
        this.task_completed = task_completed;
    }

}
