package edu.labs;

import java.util.ArrayList;
import java.util.List;
public final class TaskGroup extends TaskComponent {
    private final List<TaskComponent> children = new ArrayList<>();
    public TaskGroup(String name) { super(name); }
    @Override public List<TaskComponent> children() { return List.copyOf(children); }
    @Override public void add(TaskComponent child) {
        if (child == null) throw new IllegalArgumentException("Нельзя добавить пустой компонент");
        for (TaskComponent p = this; p != null; p = p.parent())
            if (p == child) throw new IllegalArgumentException("Нельзя создавать циклы в дереве");
        if (child.parent() != null) throw new IllegalArgumentException("Задача уже входит в другой проект");
        children.add(child); child.setParent(this);
    }
    @Override public void remove(TaskComponent child) {
        if (!children.remove(child)) throw new IllegalArgumentException("Компонент не найден в проекте");
        child.setParent(null);
    }
    @Override public double getTotalTime() { return children.stream().mapToDouble(TaskComponent::getTotalTime).sum(); }
    @Override public double getCompletedTime() { return children.stream().mapToDouble(TaskComponent::getCompletedTime).sum(); }
    @Override public int leafCount() { return children.stream().mapToInt(TaskComponent::leafCount).sum(); }
    @Override public int completedCount() { return children.stream().mapToInt(TaskComponent::completedCount).sum(); }
    @Override public void setDone(boolean done) { children.forEach(c -> c.setDone(done)); }
}
