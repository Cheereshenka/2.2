package edu.labs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class CompositeTest {
    @TempDir Path dir;
    @Test void recursivelyAggregatesLeavesInsteadOfAveragingGroups() {
        TaskGroup root = new TaskGroup("Корень"), child = new TaskGroup("Ветка"); root.add(child);
        SimpleTask a = new SimpleTask("А", 2, LocalDate.now()), b = new SimpleTask("Б", 4, LocalDate.now()), c = new SimpleTask("В", 3, LocalDate.now());
        child.add(a); child.add(b); root.add(c); a.setDone(true);
        assertEquals(9, root.getTotalTime()); assertEquals(1.0 / 3, root.getProgress(), 1e-12);
        child.setDone(true); assertEquals(2.0 / 3, root.getProgress(), 1e-12);
    }
    @Test void rejectsCyclesAndSharedChildren() {
        TaskGroup a = new TaskGroup("А"), b = new TaskGroup("Б"); a.add(b);
        assertThrows(IllegalArgumentException.class, () -> b.add(a));
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
        assertThrows(IllegalArgumentException.class, () -> a.add(a));
    }
    @Test void persistencePreservesUnicodeAndCompletedTasks() throws Exception {
        TaskGroup root = new TaskGroup("Учёба"); SimpleTask t = new SimpleTask("Зачёт", 2.5, LocalDate.of(2026, 9, 20)); root.add(t); t.setDone(true);
        Path file = dir.resolve("project.properties"); ProjectStore.save(root, file); TaskGroup restored = ProjectStore.load(file);
        assertEquals("Учёба", restored.name()); assertEquals(1, restored.getProgress()); assertEquals(2.5, restored.getTotalTime());
    }
}
