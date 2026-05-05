package com.example.os_project.scheduler;

import com.example.os_project.model.GanttRecord;
import com.example.os_project.model.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class SJFScheduler {

    public List<GanttRecord> ganttChart = new ArrayList<>();

    public void checkForNewArrivals(List<Process> processes, int n, int currentTime, Queue<Integer> readyQueue) {
        for (int i = 0; i < n; i++) {
            Process p = processes.get(i);
            if (p.arrivalTime <= currentTime && !p.inQueue && !p.isComplete) {
                p.inQueue = true;
                readyQueue.add(i);
            }
        }
    }

    public int[] updateQueue(List<Process> processes, int n, Queue<Integer> readyQueue, int currentTime, int programsExecuted) {
        int i = readyQueue.remove();

        processes.get(i).firstStartedTime = currentTime;
        processes.get(i).responseTime = currentTime - processes.get(i).arrivalTime;

        // UI Integration: Log the Gantt block
        ganttChart.add(new GanttRecord(processes.get(i).id, currentTime, currentTime + processes.get(i).burstTime));

        processes.get(i).isComplete = true;
        currentTime += processes.get(i).burstTime;
        processes.get(i).completionTime = currentTime;
        processes.get(i).turnaroundTime = processes.get(i).completionTime - processes.get(i).arrivalTime;
        processes.get(i).waitingTime = processes.get(i).turnaroundTime - processes.get(i).burstTime;
        programsExecuted++;

        if (processes.get(i).waitingTime < 0) processes.get(i).waitingTime = 0;

        if (programsExecuted != n) {
            checkForNewArrivals(processes, n, currentTime, readyQueue);
        }
        return new int[]{currentTime, programsExecuted};
    }

    public void runSJF(List<Process> processes) {
        int n = processes.size();

        processes.sort((p1, p2) -> Integer.compare(p1.arrivalTime, p2.arrivalTime));

        // Priority Queue sorted by burst time as requested
        Queue<Integer> readyQueue = new PriorityQueue<>((i1, i2) -> {
            return Integer.compare(processes.get(i1).burstTime, processes.get(i2).burstTime);
        });

        int currentTime = 0;
        int programsExecuted = 0;

        checkForNewArrivals(processes, n, currentTime, readyQueue);

        while (programsExecuted < n) {
            if (readyQueue.isEmpty()) {
                ganttChart.add(new GanttRecord("IDLE", currentTime, currentTime + 1));
                currentTime++;
                checkForNewArrivals(processes, n, currentTime, readyQueue);
                continue;
            }

            int[] result = updateQueue(processes, n, readyQueue, currentTime, programsExecuted);
            currentTime = result[0];
            programsExecuted = result[1];
        }
    }
}