package com.example.os_project;

import com.example.os_project.model.GanttRecord;
import com.example.os_project.model.Process;
import com.example.os_project.scheduler.RoundRobinScheduler;
import com.example.os_project.scheduler.SJFScheduler; // Imports your new engine!

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

    // --- NEW: Global labels so we can update the text after math runs ---
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
        // ---> ADD THIS NEW TAB <---
        Tab conclusionTab = new Tab("4. Comparison & Conclusion", buildConclusionTab());

        // Update this line to include the 4th tab
        tabPane.getTabs().addAll(setupTab, rrTab, sjfTab, conclusionTab);


        Scene scene = new Scene(tabPane, 850, 650);
        stage.setTitle("Round Robin vs SJF Simulator");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildSetupTab() {
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

        // --- UPDATED READY QUEUE VIEW ---
        VBox queueSection = new VBox(5);
        queueSection.setPadding(new Insets(10));
        Label queueLabel = new Label("Ready Queue View (RR History Snapshot):");
        queueLabel.setStyle("-fx-font-weight: bold;");

        readyQueueLogArea = new TextArea();
        readyQueueLogArea.setEditable(false);
        // 1. Give it more vertical space (changed from 100 to 250)
        readyQueueLogArea.setPrefHeight(250);

        // 2. Make the font bigger and use a clean, monospaced font so the columns line up nicely
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

            if (id.isEmpty() || arr.isEmpty() || burst.isEmpty()) {
                showAlert("Error", "All fields required."); return;
            }
            if (!arr.matches("^\\d+$") || !burst.matches("^[1-9]\\d*$")) {
                showAlert("Error", "Arrival must be >= 0. Burst must be > 0."); return;
            }
            boolean idExists = masterProcessList.stream().anyMatch(p -> p.id.equalsIgnoreCase(id));
            if (idExists) {
                showAlert("Input Error", "A process with this ID already exists."); return;
            }

            masterProcessList.add(new Process(id, Integer.parseInt(arr), Integer.parseInt(burst)));
            idInput.clear(); arrivalInput.clear(); burstInput.clear();
        });

        runButton.setOnAction(e -> runSchedulingAlgorithms());

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(quantumBox, inputBox, processTable, queueSection, runButton);
        return root;
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
        // Add the correct global labels based on which tab this is
        if (isRR) {
            averagesBox.getChildren().addAll(rrAvgWtLabel, rrAvgTatLabel, rrAvgRtLabel);
        } else {
            averagesBox.getChildren().addAll(sjfAvgWtLabel, sjfAvgTatLabel, sjfAvgRtLabel);
        }

        layout.getChildren().addAll(titleLabel, ganttLabel, ganttScroll, resultTable, averagesBox);
        return layout;
    }

    // --- UPDATED: THE SIMULATION TRIGGER ---
    private void runSchedulingAlgorithms() {
        if (masterProcessList.isEmpty()) {
            showAlert("Error", "Please add at least one process."); return;
        }
        if (!quantumInput.getText().trim().matches("^[1-9]\\d*$")) {
            showAlert("Error", "Please enter a valid Time Quantum (> 0) before running."); return;
        }
        int quantum = Integer.parseInt(quantumInput.getText().trim());

        // 1. Create a DEEP COPY of the processes for SJF so we don't mess up the RR data
        List<Process> sjfClonedList = new ArrayList<>();
        for (Process p : masterProcessList) {
            sjfClonedList.add(new Process(p.id, p.arrivalTime, p.burstTime));
        }

        // 2. Run the SJF Math!
        SJFScheduler sjfScheduler = new SJFScheduler();
        sjfScheduler.runSJF(sjfClonedList);

        // 3. Update the SJF Table Data
        sjfResultsList.clear();
        sjfResultsList.addAll(sjfClonedList);

        // 4. Calculate SJF Averages
        double totalWt = 0, totalTat = 0, totalRt = 0;
        for (Process p : sjfClonedList) {
            totalWt += p.waitingTime;
            totalTat += p.turnaroundTime;
            totalRt += p.responseTime;
        }
        int n = sjfClonedList.size();
        sjfAvgWtLabel.setText(String.format("Average WT: %.2f", (totalWt / n)));
        sjfAvgTatLabel.setText(String.format("Average TAT: %.2f", (totalTat / n)));
        sjfAvgRtLabel.setText(String.format("Average RT: %.2f", (totalRt / n)));

        // 5. Draw the SJF Gantt Chart
        drawGanttChart(sjfGanttBox, sjfScheduler.ganttChart);

        System.out.println("SJF Simulation Complete!");
        // We will add the RR code here next!
        // --- NEW: RUN THE ROUND ROBIN MATH ---

        // 1. Create ANOTHER deep copy of the original data for RR
        List<Process> rrClonedList = new ArrayList<>();
        for (Process p : masterProcessList) {
            rrClonedList.add(new Process(p.id, p.arrivalTime, p.burstTime));
        }

        // 2. Run the RR Engine
        RoundRobinScheduler rrScheduler = new RoundRobinScheduler();
        rrScheduler.runRoundRobin(rrClonedList, quantum); // Pass the quantum here!

        // 3. Update the RR Table Data
        rrResultsList.clear();
        rrResultsList.addAll(rrClonedList);

        // 4. Calculate RR Averages
        double rrTotalWt = 0, rrTotalTat = 0, rrTotalRt = 0;
        for (Process p : rrClonedList) {
            rrTotalWt += p.waitingTime;
            rrTotalTat += p.turnaroundTime;
            rrTotalRt += p.responseTime;
        }
        rrAvgWtLabel.setText(String.format("Average WT: %.2f", (rrTotalWt / n)));
        rrAvgTatLabel.setText(String.format("Average TAT: %.2f", (rrTotalTat / n)));
        rrAvgRtLabel.setText(String.format("Average RT: %.2f", (rrTotalRt / n)));

        // 5. Draw the RR Gantt Chart (reusing our awesome drawing method!)
        drawGanttChart(rrGanttBox, rrScheduler.ganttChart);

        System.out.println("Round Robin Simulation Complete!");

        // Show the queue log on the screen!
        readyQueueLogArea.setText(rrScheduler.queueLog.toString());
    }

    // --- NEW: GANTT CHART DRAWING METHOD ---
    private void drawGanttChart(HBox ganttBox, List<GanttRecord> chartData) {
        ganttBox.getChildren().clear();

        for (GanttRecord record : chartData) {
            VBox block = new VBox(2);
            block.setAlignment(Pos.CENTER);
            block.setPadding(new Insets(5));

            // If it's IDLE time, make it gray. Otherwise, make it blue.
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
    // --- 4. BUILDS THE FINAL CONCLUSION TAB ---
    private VBox buildConclusionTab() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label title = new Label("Comparison Summary Panel");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // The Side-by-Side Summary Panel
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

        // The Final Conclusion Area
        Label conclusionTitle = new Label("Final Conclusion Area:");
        conclusionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea conclusionBox = new TextArea();
        conclusionBox.setPromptText("Write your analysis here. Discuss fairness vs. efficiency, the effect of the quantum, and which algorithm performed better for this workload...");
        conclusionBox.setWrapText(true);
        VBox.setVgrow(conclusionBox, Priority.ALWAYS); // Makes the text box stretch to fill the screen

        layout.getChildren().addAll(title, summaryPanel, conclusionTitle, conclusionBox);
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