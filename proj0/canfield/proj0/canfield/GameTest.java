package canfield;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests of the Game class.
 * @author JuKyung Choi
 */

public class GameTest {

    /** Example. */
    @Test
    public void testInitialScore() {
        Game g = new Game();
        g.deal();
        assertEquals(5, g.getScore());
    }

    /** Tests of undo.*/
    @Test
    public void testUndo() {
        Game s = new Game();
        s.deal();
        s.stockToWaste();
        Card exp = s.topWaste();
        s.stockToWaste();
        s.undoMove();
        Card undo = s.topWaste();
        assertEquals(exp, undo);

        Game s2 = new Game();
        s2.deal();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 4; i++) {
                try {
                    Card exp2 = s2.topTableau(i);
                    s2.wasteToTableau(i);
                    s2.undoMove();
                    Card undo2 = s2.topTableau(i);
                    assertEquals(exp2, undo2);
                } catch (IllegalArgumentException e) {
                    s2.stockToWaste();
                }
            }
        }
    }
}
