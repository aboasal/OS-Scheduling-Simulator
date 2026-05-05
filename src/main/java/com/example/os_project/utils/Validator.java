package com.example.os_project.utils;

import com.example.os_project.model.Process;
import java.util.List;

public class Validator {

    // Checks if the user left any boxes empty
    public static boolean isInputEmpty(String id, String arr, String burst) {
        return id.isEmpty() || arr.isEmpty() || burst.isEmpty();
    }

    // Checks if the numbers are actually positive integers
    public static boolean isValidNumbers(String arr, String burst) {
        return arr.matches("^\\d+$") && burst.matches("^[1-9]\\d*$");
    }

    // Checks for duplicate IDs
    public static boolean isDuplicateId(List<Process> list, String id) {
        return list.stream().anyMatch(p -> p.id.equalsIgnoreCase(id));
    }
}