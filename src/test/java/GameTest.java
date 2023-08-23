import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class GameTest {
    @Test
    public void testReturnArg()  {
        assertEquals(1, Game.returnArg(1));
    }
}
