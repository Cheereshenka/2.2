package edu.labs;

import java.time.LocalDate;
public final class SimpleTask extends TaskComponent {
    private double hours;
    private LocalDate deadline;
    private boolean done;
    public SimpleTask(String name, double hours, LocalDate deadline) { super(name); edit(hours, deadline); }
    public void edit(double hours, LocalDate deadline) {
        if (!Double.isFinite(hours) || hours <= 0) throw new IllegalArgumentException("Время должно быть больше нуля");
        if (deadline == null) throw new IllegalArgumentException("Укажите срок задачи");
        this.hours = hours; this.deadline = deadline;
    }
    public LocalDate deadline() { return deadline; }
    public boolean isDone() { return done; }
    @Override public double getTotalTime() { return hours; }
    @Override public double getCompletedTime() { return done ? hours : 0; }
    @Override public int leafCount() { return 1; }
    @Override public int completedCount() { return done ? 1 : 0; }
    @Override public void setDone(boolean done) { this.done = done; }
}
