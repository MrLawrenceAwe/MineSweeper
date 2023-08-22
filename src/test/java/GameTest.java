import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameTest {

    private Game game;

    @BeforeEach
    public void setUp() throws InterruptedException {
        game = new Game();
    }

    @Test
    public void testExtractCoordinate_ValidInput_ReturnsCoordinate() {
        Coordinate result = game.decrementBothAxesAndExtractCoordinate("1", "2");

        assertEquals(0, result.x);
        assertEquals(1, result.y);
    }

    @Test
    public void testExtractCoordinate_InvalidX_ReturnsNull() {
        Coordinate result = game.decrementBothAxesAndExtractCoordinate("a", "2");

        assertNull(result);
    }

    @Test
    public void testExtractCoordinate_InvalidY_ReturnsNull() {
        Coordinate result = game.decrementBothAxesAndExtractCoordinate("1", "b");

        assertNull(result);
    }
}