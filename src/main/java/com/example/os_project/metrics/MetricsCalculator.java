package com.example.os_project.metrics;

import com.example.os_project.model.Process;
import java.util.List;

public class MetricsCalculator {

    public static double getAverageWT(List<Process> processes) {
        if (processes.isEmpty()) return 0;
        double total = 0;
        for (Process p : processes) total += p.waitingTime;
        return total / processes.size();
    }

    public static double getAverageTAT(List<Process> processes) {
        if (processes.isEmpty()) return 0;
        double total = 0;
        for (Process p : processes) total += p.turnaroundTime;
        return total / processes.size();
    }

    public static double getAverageRT(List<Process> processes) {
        if (processes.isEmpty()) return 0;
        double total = 0;
        for (Process p : processes) total += p.responseTime;
        return total / processes.size();
    }
}