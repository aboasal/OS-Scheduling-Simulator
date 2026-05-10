package com.example.os_project.scheduler;

import com.example.os_project.model.GanttRecord;
import com.example.os_project.model.Process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinScheduler {

    public List<GanttRecord> ganttChart = new ArrayList<>();
    public StringBuilder queueLog = new StringBuilder();

    public void checkForNewArrivals(List<Process> processes, int n, int currentTime, Queue<Integer> readyQueue) {
        for (int i = 0; i < n; i++) {
            Process p = processes.get(i);
            if (p.arrivalTime <= currentTime && !p.inQueue && !p.isComplete) {
                p.inQueue = true;
                readyQueue.add(i);
            }
        }
    }

    public int[] updateQueue(List<Process> processes, int n, int quantum, Queue<Integer> readyQueue, int currentTime, int programsExecuted) {
        int i = readyQueue.remove();


        if (processes.get(i).responseTime == -1) {
            processes.get(i).firstStartedTime = currentTime;
            processes.get(i).responseTime = currentTime - processes.get(i).arrivalTime;
        }

        int timeToRun = Math.min(processes.get(i).burstTimeRemaining, quantum);


        ganttChart.add(new GanttRecord(processes.get(i).id, currentTime, currentTime + timeToRun));

        if (processes.get(i).burstTimeRemaining <= quantum) {
            processes.get(i).isComplete = true;
            currentTime += processes.get(i).burstTimeRemaining;
            processes.get(i).completionTime = currentTime;
            processes.get(i).turnaroundTime = processes.get(i).completionTime - processes.get(i).arrivalTime;
            processes.get(i).waitingTime = processes.get(i).turnaroundTime - processes.get(i).burstTime;
            processes.get(i).burstTimeRemaining = 0;
            programsExecuted++;

            if (processes.get(i).waitingTime < 0) processes.get(i).waitingTime = 0;

            if (programsExecuted != n) {
                checkForNewArrivals(processes, n, currentTime, readyQueue);
            }
        } else {
            processes.get(i).burstTimeRemaining -= quantum;
            currentTime += quantum;
            if (programsExecuted != n) {
                checkForNewArrivals(processes, n, currentTime, readyQueue);
            }
            readyQueue.add(i);
        }
        return new int[]{currentTime, programsExecuted};
    }

    public void runRoundRobin(List<Process> processes, int quantum) {
        int n = processes.size();


        processes.sort((p1, p2) -> Integer.compare(p1.arrivalTime, p2.arrivalTime));

        Queue<Integer> readyQueue = new LinkedList<>();

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


            queueLog.append("Time ").append(currentTime).append(": [ ");
            for (Integer index : readyQueue) {
                Process p = processes.get(index);
                queueLog.append(p.id).append("(").append(p.burstTimeRemaining).append("s left) ");
            }
            queueLog.append("]\n");

            int[] result = updateQueue(processes, n, quantum, readyQueue, currentTime, programsExecuted);
            currentTime = result[0];
            programsExecuted = result[1];
        }
    }
}