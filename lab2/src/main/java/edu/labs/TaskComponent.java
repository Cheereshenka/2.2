package edu.labs;

import java.time.LocalDate;
import java.util.List;
public abstract class TaskComponent {
    private String name;
    private TaskGroup parent;
    protected TaskComponent(String name) { rename(name); }
    public final String name() { return name; }
    public final void rename(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Название не должно быть пустым");
        this.name = name.trim();
    }
    public final TaskGroup parent() { return parent; }
    final void setParent(TaskGroup parent) { this.parent = parent; }
    public abstract double getTotalTime();
    public abstract double getCompletedTime();
    public abstract int leafCount();
    public abstract int completedCount();
    public final double getProgress() { return leafCount() == 0 ? 0 : (double) completedCount() / leafCount(); }
    public abstract void setDone(boolean done);
    public List<TaskComponent> children() { return List.of(); }
    public void add(TaskComponent child) { throw new IllegalArgumentException("Простая задача не может содержать подзадачи"); }
    public void remove(TaskComponent child) { throw new IllegalArgumentException("У простой задачи нет подзадач"); }
    @Override public String toString() { return (this instanceof TaskGroup ? "▸ " : "• ") + name + "   " + Math.round(getProgress() * 100) + "%"; }
}
