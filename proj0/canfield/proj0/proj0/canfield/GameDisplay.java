package canfield;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;

/**
 * A widget that displays a Pinball playfield.
 * 
 * @author P. N. Hilfinger
 */
class GameDisplay extends Pad {

    ArrayList<GameDisplay> Display = new ArrayList<GameDisplay>();

    /** Color of display field. */
    private static final Color BACKGROUND_COLOR = Color.white;

    /* Coordinates and lengths in pixels unless otherwise stated. */

    /** Preferred dimensions of the playing surface. */
    private static final int BOARD_WIDTH = 725, BOARD_HEIGHT = 700;

    /** Displayed dimensions of a card image. */
    private static final int CARD_HEIGHT = 125, CARD_WIDTH = 90;
    static final double MOUSE_TOLERANCE = 0.5 * CARD_WIDTH;

    /** A graphical representation of GAME. */
    public GameDisplay(Game game) {
        _game = game;
        this.ty[0] = this.ty[1] = this.ty[2] = this.ty[3] = 195;
        this.tyN[0] = this.tyN[1] = this.tyN[2] = this.tyN[3] = 195;
        this.clickedTY = new int[4];
        setPreferredSize(BOARD_WIDTH, BOARD_HEIGHT);
    }

    /** Return an Image read from the resource named NAME. */
    private Image getImage(String name) {
        InputStream in = getClass().getResourceAsStream("/canfield/resources/" + name);
        try {
            return ImageIO.read(in);
        } catch (IOException excp) {
            return null;
        }
    }

    /** Return an Image of CARD. */
    private Image getCardImage(Card card) {
        return getImage("playing-cards/" + card + ".png");
    }

    /** Return an Image of the back of a card. */
    private Image getBackImage() {
        return getImage("playing-cards/blue-back.png");
    }

    /** Draw CARD at X, Y on G. */
    private void paintCard(Graphics2D g, Card card, int x, int y) {
        if (card != null) {
            g.drawImage(getCardImage(card), x, y, CARD_WIDTH, CARD_HEIGHT, null);
        }
    }

    /** Draw card back at X, Y on G. */
    private void paintBack(Graphics2D g, int x, int y) {
        g.drawImage(getBackImage(), x, y, CARD_WIDTH, CARD_HEIGHT, null);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BACKGROUND_COLOR);
        Rectangle b = g.getClipBounds();
        g.fillRect(0, 0, b.width, b.height);
        // PAINT every card from where I clicked. access pressed
        int printPile = CanfieldGUI.pileTabNum;

        // if the clicked card is the top card of its tableau
        int count1 = _game.tableauSize(1);
        for (int ta = 0; ta < _game.tableauSize(1); ta++) {
            paintCard(g, _game.getTableau(1, _game.tableauSize(1) - 1 - ta), tx[0], ty[0] - incr * (count1 - 1 - ta));
        }
        int count2 = _game.tableauSize(2);
        for (int tb = 0; tb < _game.tableauSize(2); tb++) {
            paintCard(g, _game.getTableau(2, _game.tableauSize(2) - 1 - tb), tx[1], ty[1] - incr * (count2 - 1 - tb));
        }
        int count3 = _game.tableauSize(3);
        for (int tc = 0; tc < _game.tableauSize(3); tc++) {
            paintCard(g, _game.getTableau(3, _game.tableauSize(3) - 1 - tc), tx[2], ty[2] - incr * (count3 - 1 - tc));
        }
        int count4 = _game.tableauSize(4);
        for (int td = 0; td < _game.tableauSize(4); td++) {
            paintCard(g, _game.getTableau(4, _game.tableauSize(4) - 1 - td), tx[3], ty[3] - incr * (count4 - 1 - td));
        }
        try {
            paintCard(g, _game.topReserve(), rx, ry);
        } catch (IllegalArgumentException e) {
        }
        if (_game.topWaste() != null) {
            try {
                paintCard(g, _game.topWaste(), wx, wy);
            } catch (IllegalArgumentException e) {
            }
        }
        paintBack(g, sx, sy);
        // paints the top foundation cards
        for (int a = 0; a < 4; a++) {
            if (_game.foundationSize(a + 1) > 0) {
                try {
                    paintCard(g, _game.topFoundation(a + 1), fx[a], fy[a]);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        tabToFoundFac = -1;
    }

    /**
     * NEW METHOD returns the card that is clicked
     */
    public Card findCard(int x, int y) {
        if (x > sx && x < sx + CARD_WIDTH && y > sy && y < sy + CARD_HEIGHT)
            return _game.topStock();
        else if (x > wx && x < wx + CARD_WIDTH && y > wy && y < wy + CARD_HEIGHT) {
            if (_game.topWaste() != null) {
                return _game.topWaste();
            }
        } else if (x > rx && x < rx + CARD_WIDTH && y > ry && y < ry + CARD_HEIGHT)
            return _game.topReserve();
        else {
            for (int a = 0; a < 4; a++) {
                if (x > fx[a] && x < fx[a] + CARD_WIDTH && y > fy[a] && y < fy[a] + CARD_HEIGHT) {
                    if (_game.foundationSize(a + 1) > 0) {
                        return _game.topFoundation(a + 1);
                    }
                }
                for (int pileCheck = 1; pileCheck < 5; pileCheck++) {
                    if (pileCheck == findTabPile(x, y)) {
                        if (x > tArr[pileCheck - 1] && x < tArr[pileCheck - 1] + CARD_HEIGHT && y > 195) {
                            if (y > tyN[pileCheck - 1] && y < tyN[pileCheck - 1] + CARD_HEIGHT) {
                                return _game.topTableau(pileCheck);
                            } else{
                                return _game.getTableau(pileCheck,
                                    (int) (int) Math.ceil((double) (tyN[pileCheck - 1] - y) / (double) incr));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void tabToFound1(Card pressed) {
        for (int tabtofound = 1; tabtofound < 5; tabtofound++) {
            if (pressed == _game.topTableau(tabtofound)) {
                try {
                    _game.tableauToFoundation(tabtofound);
                    if (_game.tableauSize(tabtofound) > 1) {
                        tyN[tabtofound - 1] -= incr;
                    }
                    counter++;
                    tabToFoundFac = 0;
                    break;
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    public void tabToTab(Card pressed, int prX, int prY, int relX, int relY) {
        int pPile = findTabPile(prX, prY);
        int rPile = findTabPile(relX, relY);
        if (pPile != rPile && pPile > 0 && rPile > 0) {
            try {
                int temp = _game.tableauSize(pPile);
                _game.tableauToTableau(pPile, rPile);
                tyN[rPile - 1] += temp * incr;
                tyN[pPile - 1] = 195;
                counter++;
            } catch (IllegalArgumentException e) {
            }
        }
    }


    /**
     * NEW METHOD move card from foundation to tableau.
     * PRX, PRY, RELX, RELY are pressed/released coords.
     */

    public void foundToTab(int prx, int pry, int relx, int rely) {
        int pPile = 0;
        int rPile = findTabPile(relx, rely);
        for (int i = 1; i < 5; i++) {
            if (prx > fx[i - 1] && prx < fx[i - 1] + CARD_WIDTH && pry > 20 && pry < 20 + CARD_HEIGHT) {
                pPile = i;
                break;
            }
        }
        try {
            if (rPile > 0 && pPile > 0) {
                _game.foundationToTableau(pPile, rPile);
                tyN[rPile - 1] += incr;
                counter++;
            }
        } catch (IllegalArgumentException e) {
        }
    }

	// REVISED revised VERSION OF WASTETOTAB
    public void wasteToTab(Card pressed) {
        for (int i = 1; i < 5; i++) {
            try {
                _game.wasteToTableau(i);
                tyN[i - 1] += incr;
                counter++;
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public void resToTab(Card pressed) {
        for (int i = 1; i < 5; i++) {
            try {
                _game.reserveToTableau(i);
                tyN[i - 1] += incr;
                counter++;
                break;
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public int wasteToFound(Card pressed) {
        try {
            _game.wasteToFoundation();
            counter++;
            return 0;
        } catch (IllegalArgumentException e) {
        }
        return -1;
    }

    public int resToFound(Card pressed) {
        try {
            _game.reserveToFoundation();
            counter++;
            return 0;
        } catch (IllegalArgumentException e) {
        }
        return -1;
    }

    /** remove the top tableau in pile 'i' from the pile into a new one. **/
    void topToNew(int i) {
        pil.move(_game.getTableau().get(i - 1), 1);
    }

    /**
     * Finds the tableau pile the pressed card is in and returns that number
     * returns 0 if it isn't in a tableau pile
     */
    int findTabPile(int prX, int prY) {
        for (int i = 1; i < 5; i++) {
            if (prX > tArr[i - 1] && prX < (tArr[i - 1] + CARD_WIDTH) && prY > 195
                    && prY < (tyN[i - 1]) + CARD_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the foundation pile the pressed card is in and returns that number
     * returns 0 if it isn't in a tableau pile
     */
    int findFoundPile(int prX, int prY) {
        for (int i = 1; i < 5; i++) {
            if (prX > fArr[i - 1] && prX < (fArr[i - 1] + CARD_WIDTH) && prY > fArr[4]
                    && prY < (fArr[4] + CARD_HEIGHT)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * UNDO FUNCTION FOR GUI save every instance of display in an arraylist
     * which instance variables do I need? what changes? only y coordinate of
     * tableau.
     */
    void copyFrom(GameDisplay display0) {
        ty = display0.ty;
        tyN = display0.tyN;
        clickedTY = display0.clickedTY;
    }

    Pile pil;

    /** Game I am displaying. */
    private final Game _game;
    public final int rx = 20;
    public final int ry = 195;
    public final static int sx = 20;
    public final int sy = 370;
    public final int wx = 120;
    public final int wy = 370;
    public final int incr = 30;

	// keeps track of the painting positions of TOP tableaus
    public int[] tx = { 300, 400, 500, 600 };
    // uncomment to fix later: public int[] ty = { 195, 195, 195, 195 };
    public int[] ty = new int[4];

    // keeps track of the original positions
    public int[] tArr = new int[] { 300, 400, 500, 600, 195 };
    public int[] fArr = new int[] { 300, 400, 500, 600, 20 };

    // keeps track of foundation positions
    public int[] fx = { 300, 400, 500, 600 };
    public int[] fy = { 20, 20, 20, 20 };
    public int[] tyN = new int[4];
    // uncomment to fix; public int[] tyN = { 195, 195, 195, 195 };

    // keeps track of clicked ty
    // uncomment later to fix: public int[] clickedTY = new int[4];
    public int[] clickedTY;

    // arraylist of tableau position

    int[] clickedTX = { 300, 400, 500, 600 };
    // keeps track of whether tabtofound was successful so that the right paint
    // method can be chosen accordingly
    int tabToFoundFac;

    // keeps track of which method was called on click/release. allows me to
    // make sure that only one method is called per click/release combo.
    int counter;

    int clickedX;
    int clickedY;
}
