package edu.labs;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Properties;
/** Читаемый UTF-8 properties-файл. Никакой Java-десериализации. */
public final class ProjectStore {
    public static void save(TaskGroup root, Path path) throws IOException {
        Properties p = new Properties(); encode(p, "root", root);
        try (Writer out = Files.newBufferedWriter(path)) { p.store(out, "Task tree v1"); }
    }
    private static void encode(Properties p, String key, TaskComponent item) {
        p.setProperty(key + ".name", item.name());
        p.setProperty(key + ".type", item instanceof TaskGroup ? "group" : "task");
        if (item instanceof SimpleTask t) {
            p.setProperty(key + ".hours", "" + t.getTotalTime()); p.setProperty(key + ".deadline", t.deadline().toString());
            p.setProperty(key + ".done", "" + t.isDone());
        } else {
            p.setProperty(key + ".count", "" + item.children().size());
            for (int i = 0; i < item.children().size(); i++) encode(p, key + "." + i, item.children().get(i));
        }
    }
    public static TaskGroup load(Path path) throws IOException {
        Properties p = new Properties(); try (Reader in = Files.newBufferedReader(path)) { p.load(in); }
        try {
            TaskComponent root = decode(p, "root", 0);
            if (!(root instanceof TaskGroup group)) throw new IllegalArgumentException("Корень должен быть проектом");
            return group;
        } catch (RuntimeException e) { throw new IOException("Некорректный файл проекта: " + e.getMessage(), e); }
    }
    private static TaskComponent decode(Properties p, String key, int depth) {
        if (depth > 100) throw new IllegalArgumentException("Слишком глубокое дерево");
        String name = p.getProperty(key + ".name");
        if ("task".equals(p.getProperty(key + ".type"))) {
            SimpleTask t = new SimpleTask(name, Double.parseDouble(p.getProperty(key + ".hours")), LocalDate.parse(p.getProperty(key + ".deadline")));
            t.setDone(Boolean.parseBoolean(p.getProperty(key + ".done"))); return t;
        }
        if (!"group".equals(p.getProperty(key + ".type"))) throw new IllegalArgumentException("Неизвестный тип компонента");
        TaskGroup group = new TaskGroup(name); int count = Integer.parseInt(p.getProperty(key + ".count"));
        if (count < 0 || count > 10000) throw new IllegalArgumentException("Некорректное количество элементов");
        for (int i = 0; i < count; i++) group.add(decode(p, key + "." + i, depth + 1));
        return group;
    }
}
