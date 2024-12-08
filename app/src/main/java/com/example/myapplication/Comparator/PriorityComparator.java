package com.example.myapplication.Comparator;

import com.example.myapplication.Models.Task_Model;

import java.util.Comparator;

public class PriorityComparator implements Comparator<Task_Model> {

    @Override
    public int compare(Task_Model obj1, Task_Model obj2) {
        char p1 = obj1.getTask_priority(); // Get the priority character from your data type
        char p2 = obj2.getTask_priority();

        if (p1 == 'H' && p2 != 'H') {
            return -1;
        } else if (p1 != 'H' && p2 == 'H') {
            return 1;
        } else if (p1 == 'M' && p2 == 'L'){
            return -1;
        }else if (p1 == 'L' && p2 =='M'){
            return 1;
        }else {
            return 0; // if priority is same
        }
    }
}
