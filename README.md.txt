# 🖥️ CPU Scheduling Simulator: Round Robin vs SJF

## 📖 Project Overview
This project is a JavaFX-based graphical simulator designed to compare the performance and behavior of two fundamental Operating System CPU scheduling algorithms: **Round Robin (RR)** and **Non-Preemptive Shortest Job First (SJF)**.

The simulator allows users to input custom processes, validates the data, visualizes the execution via Gantt charts, and calculates key performance metrics (Waiting Time, Turnaround Time, and Response Time) to prove the theoretical tradeoffs between fairness and efficiency.

## ✨ Key Features
*   **Dual Algorithm Simulation:** Runs both RR and Non-Preemptive SJF simultaneously on cloned datasets for perfect 1-to-1 comparison.
*   **Dynamic Gantt Charts:** Automatically generates visual time-blocks for CPU execution.
*   **Live Ready Queue Snapshot:** Provides a second-by-second history log of the Round Robin queue rotation.
*   **Robust Input Validation:** Prevents crashes by blocking negative times, zero burst times, non-numeric characters, and duplicate Process IDs.

---

## 🧪 Required Test Scenarios

### Scenario B: Short-Job-Heavy Case (Efficiency Test)
**Objective:** To demonstrate how SJF minimizes average waiting time by prioritizing shorter tasks.
*   **Data:** Quantum = 3 | P1(Burst 10), P2(Burst 2), P3(Burst 1), P4(Burst 2)
*   **Result:** SJF executes P3, P2, and P4 first, getting them out of the system rapidly.

![SJF Results](screenshots/scenario_b_sjf.png)
*(Note: Replace with your actual screenshot path)*

### Scenario C: Fairness Case (Interactivity Test)
**Objective:** To demonstrate how Round Robin prevents CPU starvation and shares resources fairly.
*   **Data:** Quantum = 2 | P1(Burst 6), P2(Burst 6), P3(Burst 6)
*   **Result:** Under SJF, P3 would have to wait 12 seconds to even start. Under Round Robin, the CPU is shared in a striped pattern, giving every process an excellent Response Time.

![Round Robin Results](screenshots/scenario_c_rr.png)
*(Note: Replace with your actual screenshot path)*

### Scenario E: Input Validation
**Objective:** To prove the system gracefully handles user errors.
*   **Test:** Attempted to add a process with an empty ID and non-numeric burst time.
*   **Result:** System blocked execution and displayed a user-friendly error dialog.

![Validation Error](screenshots/scenario_e_error.png)
*(Note: Replace with your actual screenshot path)*

---

## 📊 Final Analysis & Conclusion

1. **Which algorithm yielded the lower average waiting time?**
   Shortest Job First (SJF) yielded the lower average waiting time. By getting the shortest jobs out of the way immediately, it drastically reduces the time smaller processes spend sitting in the queue, driving the overall average down.

2. **Which algorithm yielded the lower average response time?**
   Round Robin yielded the lower average response time. Because it forces the CPU to switch contexts frequently based on the Time Quantum, no process is forced to wait for a massive job to finish before getting its first turn.

3. **How does the Time Quantum affect Round Robin?**
   The Time Quantum is the defining factor of Round Robin. If the quantum is set too high (e.g., 100), the algorithm degrades into a basic First-Come-First-Serve (FCFS) system because every process finishes within its first turn. If the quantum is set too low (e.g., 1), the CPU spends too much overhead constantly pausing and swapping processes, severely impacting performance.