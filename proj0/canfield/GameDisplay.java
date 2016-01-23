package canfield;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import java.io.InputStream;
import java.io.IOException;

/**
 * A widget that displays a Pinball playfield.
 * @author P. N. Hilfinger
 */
class GameDisplay extends Pad {

    /** Color of display field. */
    private static final Color BACKGROUND_COLOR = Color.white;

    /* Coordinates and lengths in pixels unless otherwise stated. */

    /** Preferred dimensions of the playing surface. */
    private static final int BOARD_WIDTH = 725, BOARD_HEIGHT = 700;

    /** Displayed dimensions of a card image. */
    private static final int CARD_HEIGHT = 125, CARD_WIDTH = 90;
    /** How much you can be off the mark by. */
    static final double MOUSE_TOLERANCE = 0.5 * CARD_WIDTH;

    /** A graphical representation of GAME. */
    public GameDisplay(Game game) {
        _game = game;
        this.ty[0] = this.ty[1] = this.ty[2] = this.ty[3] = TABY;
        this.clickedTY = new int[4];
        setPreferredSize(BOARD_WIDTH, BOARD_HEIGHT);
    }

    /** Return an Image read from the resource named NAME. */
    private Image getImage(String name) {
        InputStream in = getClass().getResourceAsStream("/canfield/resources/"
            + name);
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
            g.drawImage(getCardImage(card), x, y, CARD_WIDTH, CARD_HEIGHT,
                    null);
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
        int count1 = _game.tableauSize(1);
        for (int ta = 0; ta < _game.tableauSize(1); ta++) {
            paintCard(g, _game.getTableau(1, _game.tableauSize(1) - 1 - ta),
                    tx[0], ty[0] - INCR * (count1 - 1 - ta));
        }
        int count2 = _game.tableauSize(2);
        for (int tb = 0; tb < _game.tableauSize(2); tb++) {
            paintCard(g, _game.getTableau(2, _game.tableauSize(2) - 1 - tb),
                    tx[1], ty[1] - INCR * (count2 - 1 - tb));
        }
        int count3 = _game.tableauSize(3);
        for (int tc = 0; tc < _game.tableauSize(3); tc++) {
            paintCard(g, _game.getTableau(3, _game.tableauSize(3) - 1 - tc),
                    tx[2], ty[2] - INCR * (count3 - 1 - tc));
        }
        int count4 = _game.tableauSize(4);
        for (int td = 0; td < _game.tableauSize(4); td++) {
            paintCard(g, _game.getTableau(4, _game.tableauSize(4) - 1 - td),
                    tx[3], ty[3] - INCR * (count4 - 1 - td));
        }
        try {
            paintCard(g, _game.topReserve(), rx, RY);
        } catch (IllegalArgumentException e) {
            /* Ignore ILLEGALARGUMENTEXCEPTION. */
        }
        if (_game.topWaste() != null) {
            try {
                paintCard(g, _game.topWaste(), WX, WY);
            } catch (IllegalArgumentException e) {
                /* Ignore ILLEGALARGUMENTEXCEPTION. */
            }
        }
        paintBack(g, SX, SY);
        for (int a = 0; a < 4; a++) {
            if (_game.foundationSize(a + 1) > 0) {
                try {
                    paintCard(g, _game.topFoundation(a + 1), fx[a], fy[a]);
                } catch (IllegalArgumentException e) {
                    /* Ignore ILLEGALARGUMENTEXCEPTION. */
                }
            }
        }
    }

    /**
     * NEW METHOD returns the card that is clicked.
     * X coordinate, Y coordinate
     */
    public Card findCard(int x, int y) {
        if (x > SX && x < SX + CARD_WIDTH && y > SY && y < SY + CARD_HEIGHT) {
            return _game.topStock();
        } else if (x > WX && x < WX + CARD_WIDTH && y > WY
                && y < WY + CARD_HEIGHT) {
            if (_game.topWaste() != null) {
                return _game.topWaste();
            }
        } else if (x > rx && x < rx + CARD_WIDTH && y > RY && y < RY
                + CARD_HEIGHT) {
            return _game.topReserve();
        } else {
            for (int a = 0; a < 4; a++) {
                if (x > fx[a] && x < fx[a] + CARD_WIDTH && y > fy[a]
                        && y < fy[a] + CARD_HEIGHT) {
                    if (_game.foundationSize(a + 1) > 0) {
                        return _game.topFoundation(a + 1);
                    }
                }
                for (int pileCheck = 1; pileCheck < 5; pileCheck++) {
                    if (pileCheck == findTabPile(x, y)) {
                        if (x > tArr[pileCheck - 1] && x < tArr[pileCheck - 1]
                                + CARD_HEIGHT && y > TABY) {
                            if (y > TABY + _game.tableauSize(pileCheck)
                                * INCR && y < TABY + INCR
                                * _game.tableauSize(pileCheck) + CARD_HEIGHT) {
                                return _game.topTableau(pileCheck);
                            } else {
                                return _game.getTableau(pileCheck , (int) (int)
                                Math.ceil((double) (TABY + INCR
                                * (_game.tableauSize(pileCheck) - 1) - y)
                                / (double) INCR));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Moves card from tab to found.
     * @param pressed card pressed.
     */
    public void tabToFound1(Card pressed) {
        for (int tabtofound = 1; tabtofound < 5; tabtofound++) {
            if (pressed == _game.topTableau(tabtofound)) {
                try {
                    _game.tableauToFoundation(tabtofound);
                    if (_game.tableauSize(tabtofound) > 1) {
                        ty[tabtofound - 1] = TABY + INCR
                                * (_game.tableauSize(tabtofound) - 1);
                    }
                    counter++;
                    break;
                } catch (IllegalArgumentException e) {
                    /* Ignore ILLEGALARGUMENTEXCEPTION. */
                }
            }
        }
    }

    /** Moves a card from tableau to tableau.
     * @param pressed pressed card.
     * @param prX x coord pressed.
     * @param prY y coord pressed.
     * @param relX x coord released.
     * @param relY y coord released.
     */
    public void tabToTab(Card pressed, int prX, int prY, int relX, int relY) {
        int pPile = findTabPile(prX, prY);
        int rPile = findTabPile(relX, relY);
        if (pPile != rPile && pPile > 0 && rPile > 0) {
            try {
                _game.tableauToTableau(pPile, rPile);
                ty[rPile - 1] = TABY + INCR * (_game.tableauSize(rPile) - 1);
                ty[pPile - 1] = TABY + INCR * (_game.tableauSize(pPile) - 1);
                counter++;
            } catch (IllegalArgumentException e) {
                /* Ignore ILLEGALARGUMENTEXCEPTION. */
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
            if (prx > fx[i - 1] && prx < fx[i - 1] + CARD_WIDTH && pry
                    > STFOUNDY && pry < STFOUNDY + CARD_HEIGHT) {
                pPile = i;
                break;
            }
        }
        try {
            if (rPile > 0 && pPile > 0) {
                _game.foundationToTableau(pPile, rPile);
                System.out.println("moved");
                ty[rPile - 1] = TABY + INCR * (_game.tableauSize(rPile) - 1);
                counter++;
            }
        } catch (IllegalArgumentException e) {
            /* Ignore ILLEGALARGUMENTEXCEPTION. */
        }
    }

    /** Moves the waste card to a tableau pile.
     * PRESSED is card pressed.
     * RELX released x coord, RELY released y. */
    public void wasteToTab(Card pressed, int relX, int relY) {
        int rPile = findTabPile(relX, relY);
        if (rPile > 0) {
            try {
                _game.wasteToTableau(rPile);
                ty[rPile - 1] = TABY + INCR * (_game.tableauSize(rPile) - 1);
                counter++;
            } catch (IllegalArgumentException e) {
                    /* Ignore ILLEGALARGUMENTEXCEPTION. */
            }
        }
    }
    /** Moves reserve to tableau pile.
     * PRESSED card pressed.
     * RELX released x coord.
     * RELY is released y coord. */
    public void resToTab(Card pressed, int relX, int relY) {
        int rPile = findTabPile(relX, relY);
        if (rPile > 0) {
            try {
                _game.reserveToTableau(rPile);
                ty[rPile - 1] = TABY + INCR * (_game.tableauSize(rPile) - 1);
                counter++;
            } catch (IllegalArgumentException e) {
                    /* Ignore ILLEGALARGUMENTEXCEPTION. */
            }
        }
    }

    /** Moves waste card to foundation.
     * PRESSED is card pressed. */
    public void wasteToFound(Card pressed) {
        try {
            _game.wasteToFoundation();
            counter++;
        } catch (IllegalArgumentException e) {
            /* Ignore ILLEGALARGUMENTEXCEPTION. */
        }
    }
    /** Moves reserve to foundation.
     * PRESSED is card pressed */
    public void resToFound(Card pressed) {
        try {
            _game.reserveToFoundation();
            counter++;
        } catch (IllegalArgumentException e) {
            /* Ignore ILLEGALARGUMENTEXCEPTION. */
        }
    }

    /** remove the top tableau in pile 'i' from the pile into a new one.
     * I is the index.
     **/
    void topToNew(int i) {
        pil.move(_game.getTableau().get(i - 1), 1);
    }

    /**
     * Finds the tableau pile the pressed card is in and returns that number.
     * @return 0 if it isn't in a tableau pile.
     * PRX is x coord of card pressed. PRY is y coord.
     */
    int findTabPile(int prX, int prY) {
        for (int i = 1; i < 5; i++) {
            if (prX > tArr[i - 1] && prX < (tArr[i - 1] + CARD_WIDTH) && prY
                    > TABY && prY < (ty[i - 1]) + CARD_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the foundation pile the pressed card is in and returns that number.
     * returns 0 if it isn't in a tableau pile.
     * PRX is x coord of card pressed. PRY is y coord.
     */
    int findFoundPile(int prX, int prY) {
        for (int i = 1; i < 5; i++) {
            if (prX > fArr[i - 1] && prX < (fArr[i - 1] + CARD_WIDTH) && prY
                    > fArr[4] && prY < (fArr[4] + CARD_HEIGHT)) {
                return i;
            }
        }
        return -1;
    }

    /** pil is a pile. */
    private Pile pil;
    /** Starting foundation y coordinate. */
    private static final int STFOUNDY = 20;
    /** Game I am displaying. */
    private final Game _game;
    /** Reserve x coordinate. */
    private final int rx = STFOUNDY;
    /** Reserve y coordinate. */
    private static final int RY = 195;
    /** Stock x coordinate. */
    private static final int SX = STFOUNDY;
    /** Stock y coordinate. */
    private static final int SY = 370;
    /** Waste x coordinate. */
    private static final int WX = 120;
    /** Waste y coordinate. */
    private static final int WY = 370;
    /** Distance between cards in tableau. */
    private static final int INCR = 30;
    /** X coordinate of the first tableau pile. */
    private static final int TX0 = 300;
    /** X coordinate of the fourth tableau pile. */
    private static final int TX1 = 400;
    /** X coordinate of the third tableau pile. */
    private static final int TX2 = 500;
    /** X coordinate of the last tableau pile. */
    private static final int TX3 = 600;
    /** Keeps track of original x tableau coordinates. */
    private int[] tArr = new int[] {TX0, TX1, TX2, TX3};
    /** Starting tableau y coordinate. */
    private static final int TABY = RY;
    /** Keeps track of the painting x coord of TOP tableaus. */
    private int[] tx = {TX0, TX1, TX2, TX3};
    /**
     * Returns the tableau y coordinate to be printed.
     * @return tx.
     */
    int[] gettx() {
        return tx;
    }
    /**
     * Sets tx to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void settx(int i, int n) {
        tx[i] = n;
    }
    /** Keeps track of the painting y coord of TOP tableaus. */
    private int[] ty = {TABY, TABY, TABY, TABY};
    /**
     * Returns the tableau y coordinate to be painted.
     * @return ty.
     */
    int[] getty() {
        return ty;
    }
    /**
     * Sets fx to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void setty(int i, int n) {
        ty[i] = n;
    }


    /**
     * Returns the original tableau x coordinate.
     * @return tArr.
     */
    int[] gettArr() {
        return tArr;
    }
    /**
     * Sets tArr to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void settArr(int i, int n) {
        tArr[i] = n;
    }
    /** Keeps track of orginal x foundation coordinates. */
    private int[] fArr = new int[] { TX0, TX1, TX2, TX3, STFOUNDY};
    /**
     * Returns the original foundation x coordinate.
     * @return fArr.
     */
    int[] getfArr() {
        return fArr;
    }

    /** Keeps track of foundation x coordinates. */
    private int[] fx = {TX0, TX1, TX2, TX3};
    /**
     * Returns the clicked foundation x coordinate.
     * @return fx.
     */
    int[] getfx() {
        return fx;
    }
    /**
     * Sets fx to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void setfx(int i, int n) {
        fx[i] = n;
    }
    /** Keeps track of foundation x coordinates. */
    private int[] fy = {STFOUNDY, STFOUNDY, STFOUNDY, STFOUNDY};

    /**
     * Returns the clicked foundation ycoordinate.
     * @return fy.
     */
    int[] getfy() {
        return fy;
    }
    /**
     * Sets fy to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void setfy(int i, int n) {
        fy[i] = n;
    }
    /** Keeps track of clickedTY. */
    private int[] clickedTY;
    /**
     * Returns the clicked tableau ycoordinate.
     * @return clickedTY.
     */
    int[] getClickedTY() {
        return clickedTY;
    }
    /**
     * Sets clickedTY to n.
     * @param n the integer set to.
     * @param i the index.
     */
    void setClickedTY(int i, int n) {
        clickedTY[i] = n;
    }
    /** Count of how many methods were called in call/release combination. */
    private int counter;
    /**
     * Returns the counter.
     * @return counter.
     */
    int getCounter() {
        return counter;
    }
    /**
     * Sets the counter value to n.
     * @param n the integer set to.
     */
    void setCounter(int n) {
        counter = n;
    }

    /** Pressed x coordinate. */
    private int clickedX;
    /**
     * Returns the y coordinate clicked.
     * @return clicked y coordinate.
     */
    int getClickedX() {
        return clickedX;
    }
    /**
     * Sets the clickedX value to n.
     * @param n the integer set to.
     */
    void setClickedX(int n) {
        clickedX = n;
    }
    /** Clicked y coordinate. */
    private int clickedY;
    /**
     * Returns the y coordinate clicked.
     * @return clicked y coordinate.
     */
    int getClickedY() {
        return clickedY;
    }
    /**
     * Sets the clickedY value to n.
     * @param n the integer set to.
     */
    void setClickedY(int n) {
        clickedY = n;
    }
}
