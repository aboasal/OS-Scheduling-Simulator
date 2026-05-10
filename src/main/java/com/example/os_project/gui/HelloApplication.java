package com.example.os_project.gui;

import com.example.os_project.metrics.MetricsCalculator;
import com.example.os_project.utils.Validator;
import com.example.os_project.model.GanttRecord;
import com.example.os_project.model.Process;
import com.example.os_project.scheduler.RoundRobinScheduler;
import com.example.os_project.scheduler.SJFScheduler;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    private ObservableList<Process> masterProcessList = FXCollections.observableArrayList();
    private ObservableList<Process> rrResultsList = FXCollections.observableArrayList();
    private ObservableList<Process> sjfResultsList = FXCollections.observableArrayList();

    private TextArea readyQueueLogArea;
    private HBox rrGanttBox;
    private HBox sjfGanttBox;
    private TextField quantumInput;

    private Label rrAvgWtLabel = new Label("Average WT: 0.0");
    private Label rrAvgTatLabel = new Label("Average TAT: 0.0");
    private Label rrAvgRtLabel = new Label("Average RT: 0.0");

    private Label sjfAvgWtLabel = new Label("Average WT: 0.0");
    private Label sjfAvgTatLabel = new Label("Average TAT: 0.0");
    private Label sjfAvgRtLabel = new Label("Average RT: 0.0");

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab setupTab = new Tab("1. Input & Setup", buildSetupTab());
        Tab rrTab = new Tab("2. Round Robin Results", buildResultTab("Round Robin", rrResultsList, true));
        Tab sjfTab = new Tab("3. SJF Results", buildResultTab("Shortest Job First", sjfResultsList, false));
        Tab conclusionTab = new Tab("4. Comparison & Conclusion", buildConclusionTab());
        Tab analysisTab = new Tab("5. Analysis & Final Comparison", buildAnalysisTab());

        tabPane.getTabs().addAll(setupTab, rrTab, sjfTab, conclusionTab, analysisTab);

        Scene scene = new Scene(tabPane, 850, 700); // Slightly increased height for the new buttons
        stage.setTitle("Round Robin vs SJF Simulator");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildSetupTab() {
        // --- NEW: PRESET TEST CASE BUTTONS ---
        HBox presetBox = new HBox(10);
        presetBox.setPadding(new Insets(10));
        presetBox.setAlignment(Pos.CENTER_LEFT);

        Label presetLabel = new Label("Load Test Case:");
        presetLabel.setStyle("-fx-font-weight: bold;");

        Button btnA = new Button("Scenario A (Mixed)");
        Button btnB = new Button("Scenario B (Efficiency)");
        Button btnC = new Button("Scenario C (Fairness)");
        Button btnD = new Button("Scenario D (Convoy)");
        Button btnClear = new Button("Clear All");
        btnClear.setStyle("-fx-text-fill: red;");

        // Wire up the preset buttons
        btnA.setOnAction(e -> loadPreset(4,
                new Process("P1", 0, 5), new Process("P2", 1, 8), new Process("P3", 3, 2), new Process("P4", 5, 6)));

        btnB.setOnAction(e -> loadPreset(3,
                new Process("P1", 0, 10), new Process("P2", 0, 2), new Process("P3", 0, 1), new Process("P4", 0, 2)));

        btnC.setOnAction(e -> loadPreset(2,
                new Process("P1", 0, 6), new Process("P2", 0, 6), new Process("P3", 0, 6)));

        btnD.setOnAction(e -> loadPreset(3,
                new Process("P1", 0, 20), new Process("P2", 1, 2), new Process("P3", 2, 2)));

        btnClear.setOnAction(e -> {
            masterProcessList.clear();
            quantumInput.clear();
            readyQueueLogArea.clear();
        });

        presetBox.getChildren().addAll(presetLabel, btnA, btnB, btnC, btnD, btnClear);
        // -------------------------------------

        HBox quantumBox = new HBox(10);
        quantumBox.setPadding(new Insets(10));
        Label quantumLabel = new Label("Time Quantum (RR):");
        quantumInput = new TextField();
        quantumInput.setPromptText("e.g., 2");
        quantumBox.getChildren().addAll(quantumLabel, quantumInput);

        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        TextField idInput = new TextField(); idInput.setPromptText("ID (e.g., P1)");
        TextField arrivalInput = new TextField(); arrivalInput.setPromptText("Arrival");
        TextField burstInput = new TextField(); burstInput.setPromptText("Burst");
        Button addButton = new Button("Add Process");

        inputBox.getChildren().addAll(idInput, arrivalInput, burstInput, addButton);

        TableView<Process> processTable = new TableView<>();
        processTable.setItems(masterProcessList);
        TableColumn<Process, String> idCol = new TableColumn<>("Process ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id));
        TableColumn<Process, Integer> arrCol = new TableColumn<>("Arrival Time");
        arrCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().arrivalTime).asObject());
        TableColumn<Process, Integer> burstCol = new TableColumn<>("Burst Time");
        burstCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().burstTime).asObject());
        processTable.getColumns().addAll(idCol, arrCol, burstCol);
        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox queueSection = new VBox(5);
        queueSection.setPadding(new Insets(10));
        Label queueLabel = new Label("Ready Queue View (RR History Snapshot):");
        queueLabel.setStyle("-fx-font-weight: bold;");

        readyQueueLogArea = new TextArea();
        readyQueueLogArea.setEditable(false);
        readyQueueLogArea.setPrefHeight(250);
        readyQueueLogArea.setStyle("-fx-font-size: 16px; -fx-font-family: 'Consolas'; -fx-padding: 10;");
        readyQueueLogArea.setPromptText("Queue history will appear here after running...");

        queueSection.getChildren().addAll(queueLabel, readyQueueLogArea);

        Button runButton = new Button("▶ RUN SIMULATION");
        runButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        runButton.setMaxWidth(Double.MAX_VALUE);

        addButton.setOnAction(e -> {
            String id = idInput.getText().trim();
            String arr = arrivalInput.getText().trim();
            String burst = burstInput.getText().trim();

            if (Validator.isInputEmpty(id, arr, burst)) {
                showAlert("Error", "All fields required."); return;
            }
            if (!Validator.isValidNumbers(arr, burst)) {
                showAlert("Error", "Arrival must be >= 0. Burst must be > 0."); return;
            }
            if (Validator.isDuplicateId(masterProcessList, id)) {
                showAlert("Input Error", "A process with this ID already exists."); return;
            }

            masterProcessList.add(new Process(id, Integer.parseInt(arr), Integer.parseInt(burst)));
            idInput.clear(); arrivalInput.clear(); burstInput.clear();
        });

        runButton.setOnAction(e -> runSchedulingAlgorithms());

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        // ADDED presetBox TO THE TOP OF THE UI
        root.getChildren().addAll(presetBox, quantumBox, inputBox, processTable, queueSection, runButton);
        return root;
    }

    // --- NEW HELPER METHOD FOR PRESET BUTTONS ---
    private void loadPreset(int quantum, Process... processes) {
        masterProcessList.clear();
        quantumInput.setText(String.valueOf(quantum));
        masterProcessList.addAll(processes);
        readyQueueLogArea.clear();
    }

    private VBox buildResultTab(String title, ObservableList<Process> resultData, boolean isRR) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label titleLabel = new Label(title + " Results");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label ganttLabel = new Label("Gantt Chart:");
        HBox ganttContainer = new HBox(2);
        ganttContainer.setMinHeight(80);
        ganttContainer.setStyle("-fx-border-color: black; -fx-padding: 5; -fx-background-color: white;");
        ScrollPane ganttScroll = new ScrollPane(ganttContainer);
        ganttScroll.setFitToHeight(true);
        ganttScroll.setMinViewportHeight(90);

        if (isRR) rrGanttBox = ganttContainer;
        else sjfGanttBox = ganttContainer;

        TableView<Process> resultTable = new TableView<>();
        resultTable.setItems(resultData);

        TableColumn<Process, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().id));
        TableColumn<Process, Integer> wtCol = new TableColumn<>("Waiting Time (WT)");
        wtCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().waitingTime).asObject());
        TableColumn<Process, Integer> tatCol = new TableColumn<>("Turnaround Time (TAT)");
        tatCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().turnaroundTime).asObject());
        TableColumn<Process, Integer> rtCol = new TableColumn<>("Response Time (RT)");
        rtCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().responseTime).asObject());

        resultTable.getColumns().addAll(idCol, wtCol, tatCol, rtCol);
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox averagesBox = new HBox(20);
        if (isRR) {
            averagesBox.getChildren().addAll(rrAvgWtLabel, rrAvgTatLabel, rrAvgRtLabel);
        } else {
            averagesBox.getChildren().addAll(sjfAvgWtLabel, sjfAvgTatLabel, sjfAvgRtLabel);
        }

        layout.getChildren().addAll(titleLabel, ganttLabel, ganttScroll, resultTable, averagesBox);
        return layout;
    }

    private void runSchedulingAlgorithms() {
        if (masterProcessList.isEmpty()) {
            showAlert("Error", "Please add at least one process."); return;
        }
        if (!quantumInput.getText().trim().matches("^[1-9]\\d*$")) {
            showAlert("Error", "Please enter a valid Time Quantum (> 0) before running."); return;
        }
        int quantum = Integer.parseInt(quantumInput.getText().trim());

        List<Process> sjfClonedList = new ArrayList<>();
        for (Process p : masterProcessList) {
            sjfClonedList.add(new Process(p.id, p.arrivalTime, p.burstTime));
        }

        SJFScheduler sjfScheduler = new SJFScheduler();
        sjfScheduler.runSJF(sjfClonedList);

        sjfResultsList.clear();
        sjfResultsList.addAll(sjfClonedList);

        sjfAvgWtLabel.setText(String.format("Average WT: %.2f", MetricsCalculator.getAverageWT(sjfClonedList)));
        sjfAvgTatLabel.setText(String.format("Average TAT: %.2f", MetricsCalculator.getAverageTAT(sjfClonedList)));
        sjfAvgRtLabel.setText(String.format("Average RT: %.2f", MetricsCalculator.getAverageRT(sjfClonedList)));

        drawGanttChart(sjfGanttBox, sjfScheduler.ganttChart);

        List<Process> rrClonedList = new ArrayList<>();
        for (Process p : masterProcessList) {
            rrClonedList.add(new Process(p.id, p.arrivalTime, p.burstTime));
        }

        RoundRobinScheduler rrScheduler = new RoundRobinScheduler();
        rrScheduler.runRoundRobin(rrClonedList, quantum);

        rrResultsList.clear();
        rrResultsList.addAll(rrClonedList);

        rrAvgWtLabel.setText(String.format("Average WT: %.2f", MetricsCalculator.getAverageWT(rrClonedList)));
        rrAvgTatLabel.setText(String.format("Average TAT: %.2f", MetricsCalculator.getAverageTAT(rrClonedList)));
        rrAvgRtLabel.setText(String.format("Average RT: %.2f", MetricsCalculator.getAverageRT(rrClonedList)));

        drawGanttChart(rrGanttBox, rrScheduler.ganttChart);

        readyQueueLogArea.setText(rrScheduler.queueLog.toString());
    }

    private void drawGanttChart(HBox ganttBox, List<GanttRecord> chartData) {
        ganttBox.getChildren().clear();

        for (GanttRecord record : chartData) {
            VBox block = new VBox(2);
            block.setAlignment(Pos.CENTER);
            block.setPadding(new Insets(5));

            if (record.processId.equals("IDLE")) {
                block.setStyle("-fx-border-color: black; -fx-background-color: #d3d3d3; -fx-min-width: 40px;");
            } else {
                block.setStyle("-fx-border-color: black; -fx-background-color: #add8e6; -fx-min-width: 50px;");
            }

            Label idLabel = new Label(record.processId);
            idLabel.setStyle("-fx-font-weight: bold;");
            Label timeLabel = new Label(record.startTime + " - " + record.endTime);
            timeLabel.setStyle("-fx-font-size: 10px;");

            block.getChildren().addAll(idLabel, timeLabel);
            ganttBox.getChildren().add(block);
        }
    }

    private VBox buildConclusionTab() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label title = new Label("Comparison Summary Panel");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox summaryPanel = new HBox(50);
        summaryPanel.setStyle("-fx-border-color: gray; -fx-padding: 15; -fx-background-color: #f8f9fa;");

        VBox rrSummary = new VBox(10);
        Label rrTitle = new Label("Round Robin Averages");
        rrTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        rrSummary.getChildren().addAll(rrTitle, rrAvgWtLabel, rrAvgTatLabel, rrAvgRtLabel);

        VBox sjfSummary = new VBox(10);
        Label sjfTitle = new Label("SJF Averages");
        sjfTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        sjfSummary.getChildren().addAll(sjfTitle, sjfAvgWtLabel, sjfAvgTatLabel, sjfAvgRtLabel);

        summaryPanel.getChildren().addAll(rrSummary, sjfSummary);

        Label conclusionTitle = new Label("Conclusion:");
        conclusionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea conclusionBox = new TextArea();
        conclusionBox.setText("Metrics:\n" +
                "• Average Waiting Time: SJF is mostly better, except in case of convoy effect\n" +
                "• Average Turnaround Time: SJF is mostly better, except in case of convoy effect\n" +
                "• Average Response Time: RR is better, because it distributes CPU time fairly across processes\n" +
                "• Fairness: RR is fairer, more balanced than SJF\n" +
                "• Efficiency: SJF is more efficient in processing short jobs but may delay long ones.\n\n" +
                "Effect of the selected quantum:\n" +
                "• If the quantum time is very large, the RR behaves like FCFS and no switches occur.\n" +
                "• If the quantum time is too small, switching occurs a lot, reducing the efficiency significantly.\n" +
                "• So, time quantum must be kept moderate.");
        conclusionBox.setEditable(false);
        conclusionBox.setWrapText(true);
        // CHANGED FONT SIZE TO 14px
        conclusionBox.setStyle("-fx-control-inner-background: #f5f5f5; -fx-font-size: 14px;");
        VBox.setVgrow(conclusionBox, Priority.ALWAYS);

        layout.getChildren().addAll(title, summaryPanel, conclusionTitle, conclusionBox);
        return layout;
    }

    private VBox buildAnalysisTab() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label title = new Label("Analysis & Final Comparison");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label analysisLabel = new Label("Detailed Analysis:");
        analysisLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea analysisBox = new TextArea();
        analysisBox.setText("1. Which algorithm gave lower average waiting time?\n" +
                "SJF gave the lower average waiting time because it executes short processes first, reducing the time processes spent waiting in the ready queue.\n\n" +
                "2. Which algorithm gave lower average response time?\n" +
                "RR gave the lower average response time because every process receives CPU access quickly through time slicing.\n\n" +
                "3. Did Round Robin appear fairer across all processes?\n" +
                "Yes. Round Robin appeared fairer because CPU time was distributed equally among processes\n\n" +
                "4. Did SJF complete short jobs more efficiently?\n" +
                "Yes. SJF completed short jobs more efficiently by prioritizing processes with the smallest burst time, which reduced waiting and turnaround times.\n\n" +
                "5. How did the chosen quantum affect Round Robin behavior?\n" +
                "The Time Quantum strongly affected RR performance:\n" +
                "  - Large quantum will result in RR behaving similarly to FCFS.\n" +
                "  - Small quantum will result in more context switching overhead.\n" +
                "  - Balanced quantum will result in better fairness and responsiveness.\n\n" +
                "6. Which algorithm would you recommend for the tested workload, and why?\n" +
                "I would recommend RR in systems where fairness is necessary, and I would recommend SJF in systems where efficiency is needed as mostly the waiting time will be low compared to RR.");
        analysisBox.setEditable(false);
        analysisBox.setWrapText(true);
        // CHANGED FONT SIZE TO 14px
        analysisBox.setStyle("-fx-control-inner-background: #f5f5f5; -fx-font-size: 14px;");
        analysisBox.setPrefHeight(200);
        VBox.setVgrow(analysisBox, Priority.ALWAYS);

        Label comparisonLabel = new Label("Final Comparison:");
        comparisonLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea comparisonBox = new TextArea();
        comparisonBox.setText("1. Fairness vs Efficiency:\n" +
                "  - Round Robin provides better fairness for the process than the SJF as it divides the CPU time fairly across the processes\n" +
                "  - SJF produced better efficiency as shown in scenario B\n\n" +
                "2. Effect of Time Quantum on Round Robin:\n" +
                "  - If the quantum time is very large, the RR behaves like FCFS and no switches occur.\n" +
                "  - If the quantum time is too small, switching occurs a lot, reducing the efficiency significantly.\n" +
                "  - So, time quantum must be kept moderate.\n\n" +
                "3. Effect on Response Time and Long Processes:\n" +
                "  - In RR, long jobs receives CPU access regularly and every process gets quick response time.\n" +
                "  - In SJF, long jobs can wait long time before accessing the cpu, but short jobs has very fast response time.\n\n" +
                "4. How Round Robin Distributes CPU Time:\n" +
                "  - Round Robin distributes CPU time equally in a circular queue as shown in the gantt charts.\n\n" +
                "5. How SJF Favors Short Jobs:\n" +
                "  - SJF always executes the smallest available burst first as shown in the gantt charts.");
        comparisonBox.setEditable(false);
        comparisonBox.setWrapText(true);
        // CHANGED FONT SIZE TO 14px
        comparisonBox.setStyle("-fx-control-inner-background: #f5f5f5; -fx-font-size: 14px;");
        comparisonBox.setPrefHeight(200);
        VBox.setVgrow(comparisonBox, Priority.ALWAYS);

        layout.getChildren().addAll(title, analysisLabel, analysisBox, comparisonLabel, comparisonBox);
        return layout;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}