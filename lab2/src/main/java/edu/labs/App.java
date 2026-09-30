package edu.labs;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.time.LocalDate;
import java.io.File;

public class App extends Application {
    private TaskGroup root;
    private final TreeView<TaskComponent> tree = new TreeView<>();
    private final TextField name = new TextField(), hours = new TextField("1");
    private final DatePicker deadline = new DatePicker(LocalDate.now().plusDays(7));
    private final CheckBox done = new CheckBox("Выполнено");
    private final Label stats = Ui.label("", "section"), status = new Label("Выберите проект, чтобы добавить в него задачи");
    private final ProgressBar progress = new ProgressBar();
    @Override public void start(Stage stage) {
        root = new TaskGroup("Учебный семестр"); TaskGroup labs = new TaskGroup("Лабораторные работы"); root.add(labs);
        SimpleTask first = new SimpleTask("Изучить паттерны", 2, LocalDate.now().plusDays(2)); first.setDone(true); labs.add(first);
        labs.add(new SimpleTask("Подготовить приложение", 6, LocalDate.now().plusDays(7)));
        root.add(new SimpleTask("Оформить отчёт", 2, LocalDate.now().plusDays(10)));
        tree.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> { if (b != null) fill(b.getValue()); });
        VBox left = Ui.card(Ui.label("Структура проекта", "section"), tree); VBox.setVgrow(tree, Priority.ALWAYS); HBox.setHgrow(left, Priority.ALWAYS);
        Button addGroup = Ui.button("+ Подпроект", () -> { TaskComponent parent = selected(); TaskGroup child = new TaskGroup(name.getText()); parent.add(child); rebuild(child); });
        Button addTask = Ui.primary("+ Задача", () -> { TaskComponent parent = selected(); SimpleTask child = new SimpleTask(name.getText(), parseHours(), deadline.getValue()); child.setDone(done.isSelected()); parent.add(child); rebuild(child); });
        Button save = Ui.primary("Применить изменения", () -> {
            TaskComponent item = selected(); if (name.getText().isBlank()) throw new IllegalArgumentException("Введите название");
            if (item instanceof SimpleTask t) { t.edit(parseHours(), deadline.getValue()); t.setDone(done.isSelected()); }
            item.rename(name.getText()); rebuild(item);
        });
        Button delete = Ui.button("Удалить выбранное", () -> {
            TaskComponent item = selected(); if (item == root) throw new IllegalArgumentException("Корневой проект удалять нельзя");
            TaskGroup parent = item.parent(); parent.remove(item); rebuild(parent);
        }); delete.getStyleClass().add("danger");
        VBox right = Ui.card(Ui.label("Свойства элемента", "section"), Ui.field("Название / имя нового элемента", name), Ui.field("Оценка времени, часы", hours), Ui.field("Срок простой задачи", deadline), done, save,
            new Separator(), new HBox(10, addGroup, addTask), Ui.label("Для добавления выберите проект в дереве и введите название нового элемента.", "muted"),
            Ui.button("Завершить всю ветвь", () -> { TaskComponent item = selected(); item.setDone(true); rebuild(item); }),
            Ui.button("Вернуть ветвь в работу", () -> { TaskComponent item = selected(); item.setDone(false); rebuild(item); }), delete);
        right.setPrefWidth(360); right.setMinWidth(340);
            progress.setMaxWidth(Double.MAX_VALUE); progress.setMinHeight(12); progress.setPrefHeight(12);
        HBox files = new HBox(10, Ui.button("Открыть проект", () -> {
            File f = chooser().showOpenDialog(stage); if (f != null) try { root = ProjectStore.load(f.toPath()); rebuild(root); status.setText("Открыт: " + f.getName()); } catch (Exception ex) { Ui.error(ex); }
        }), Ui.button("Сохранить проект", () -> {
            File f = chooser().showSaveDialog(stage); if (f != null) try { ProjectStore.save(root, f.toPath()); status.setText("Сохранён: " + f.getName()); } catch (Exception ex) { Ui.error(ex); }
        }));
            ScrollPane editorScroll = new ScrollPane(right); editorScroll.setFitToWidth(true); editorScroll.setPrefWidth(380); editorScroll.setMinWidth(360);
            HBox body = new HBox(20, left, editorScroll); VBox.setVgrow(body, Priority.ALWAYS);
        VBox center = new VBox(16, Ui.card(stats, progress, files), body);
        Ui.show(stage, "Дерево задач", "Лабораторная 02 / Компоновщик / Вариант 2", center, status); rebuild(root);
    }
    private double parseHours() { try { return Double.parseDouble(hours.getText().replace(',', '.')); } catch (NumberFormatException e) { throw new IllegalArgumentException("Введите число часов"); } }
    private TaskComponent selected() { if (tree.getSelectionModel().getSelectedItem() == null) throw new IllegalArgumentException("Выберите элемент дерева"); return tree.getSelectionModel().getSelectedItem().getValue(); }
    private void fill(TaskComponent item) {
        name.setText(item.name()); boolean leaf = item instanceof SimpleTask;
        hours.setDisable(!leaf); deadline.setDisable(!leaf); done.setDisable(!leaf);
        // Поля доступны и для создания нового листа внутри проекта.
        if (item instanceof TaskGroup) { hours.setDisable(false); deadline.setDisable(false); done.setDisable(false); hours.setText("1"); done.setSelected(false); }
        if (item instanceof SimpleTask t) { hours.setText("" + t.getTotalTime()); deadline.setValue(t.deadline()); done.setSelected(t.isDone()); }
    }
    private TreeItem<TaskComponent> item(TaskComponent c, TaskComponent selected) {
        TreeItem<TaskComponent> node = new TreeItem<>(c); node.setExpanded(true);
        for (TaskComponent child : c.children()) node.getChildren().add(item(child, selected)); return node;
    }
    private void rebuild(TaskComponent selected) {
        tree.setRoot(item(root, selected)); select(tree.getRoot(), selected);
        stats.setText("Выполнено " + root.completedCount() + " из " + root.leafCount() + " задач · План: " + root.getTotalTime() + " ч · Завершено: " + root.getCompletedTime() + " ч");
        progress.setProgress(root.getProgress());
    }
    private void select(TreeItem<TaskComponent> node, TaskComponent selected) {
        if (node.getValue() == selected) tree.getSelectionModel().select(node); for (TreeItem<TaskComponent> child : node.getChildren()) select(child, selected);
    }
    private FileChooser chooser() { FileChooser c = new FileChooser(); c.setInitialFileName("project.properties"); c.getExtensionFilters().add(new FileChooser.ExtensionFilter("Дерево задач", "*.properties")); return c; }
    public static void main(String[] args) { launch(args); }
}
