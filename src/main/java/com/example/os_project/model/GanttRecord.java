package com.example.os_project.model;

public class GanttRecord {
    public String processId;
    public int startTime;
    public int endTime;

    public GanttRecord(String processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}