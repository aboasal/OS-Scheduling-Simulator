package com.example.os_project.model;

public class Process {
    public String id;
    public int arrivalTime;
    public int burstTime;
    public int burstTimeRemaining;
    public int firstStartedTime;
    public int responseTime;
    public int completionTime;
    public int turnaroundTime;
    public int waitingTime;

    public boolean isComplete;
    public boolean inQueue;

    public Process() {}

    public Process(String id, int arr, int brst) {
        this.id = id;
        this.arrivalTime = arr;
        this.burstTime = brst;
        this.burstTimeRemaining = brst;


        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = -1;
        this.isComplete = false;
        this.inQueue = false;
    }
}