package edu.labs;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.nio.file.Path;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

/** Необязательная проверка с реальным JavaFX-окном: -Djavafx.smoke=true. */
@EnabledIfSystemProperty(named = "javafx.smoke", matches = "true")
class UiSmokeTest {
    @Test void opensWindowAndRendersControls() throws Exception {
        
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS));
        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                App app = new App(); app.start(stage);
                
assertNotNull(stage.getScene().lookup(".tree-view"));
var tree = (javafx.scene.control.TreeView<?>) stage.getScene().lookup(".tree-view");
assertEquals(2, tree.getRoot().getChildren().size());

                stage.getScene().getRoot().applyCss(); stage.getScene().getRoot().layout();
                var image = stage.getScene().snapshot(null);
                BufferedImage png = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < png.getHeight(); y++) for (int x = 0; x < png.getWidth(); x++) png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                ImageIO.write(png, "png", Path.of("target", "ui-preview.png").toFile());
                done.complete(null);
            } catch (Throwable ex) { done.completeExceptionally(ex); }
            finally { stage.close(); }
        });
        try { done.get(30, TimeUnit.SECONDS); } finally { Platform.exit(); }
    }
    private static Button button(Stage stage, String text) {
        return stage.getScene().getRoot().lookupAll(".button").stream().filter(n -> n instanceof Button b && b.getText().equals(text)).map(n -> (Button)n).findFirst().orElseThrow();
    }
    private static Object field(App app, String name) throws Exception { var field = App.class.getDeclaredField(name); field.setAccessible(true); return field.get(app); }
}
