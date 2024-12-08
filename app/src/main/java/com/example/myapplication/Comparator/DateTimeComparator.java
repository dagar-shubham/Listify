package com.example.myapplication.Comparator;

import com.example.myapplication.Models.Task_Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class DateTimeComparator implements Comparator<Task_Model> {

    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM hh:mm aa", Locale.getDefault()); // Full format (DD MMM HH:MM AM/PM)
    private SimpleDateFormat partialDateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault()); // Partial format (DD MMM)

    @Override
    public int compare(Task_Model task1, Task_Model task2) {
        String dateString1 = task1.getTask_deadline(); // Get date string from your task model
        String dateString2 = task2.getTask_deadline(); // Get date string from your task model

        if (dateString1.isEmpty() && dateString2.isEmpty()) {
            return 0; // Both are empty, consider them equal
        } else if (dateString1.isEmpty()) {
            return 1; // Empty string comes after a valid date string
        } else if (dateString2.isEmpty()) {
            return -1; // Valid date string comes before an empty string
        }

        try {
            // Try parsing with full format first
            Date date1 = fullDateFormat.parse(dateString1);
            Date date2 = fullDateFormat.parse(dateString2);

            // If parsing is successful, compare dates
            return date1.compareTo(date2);
        } catch (ParseException e1) {
            // If full format parsing fails, try partial format
            try {
                Date date1 = partialDateFormat.parse(dateString1);
                Date date2 = partialDateFormat.parse(dateString2);

                // If both parsed successfully with partial format
                if (date1.compareTo(date2) == 0) {
                    // If base dates are equal, consider full date as "later"
                    return (dateString1.length() > dateString2.length()) ? -1 : 1;
                } else {
                    return date1.compareTo(date2);
                }
            } catch (ParseException e2) {
                // Handle parsing exceptions gracefully (e.g., return 0 for equal)
                return 0;
            }
        }
    }
}
