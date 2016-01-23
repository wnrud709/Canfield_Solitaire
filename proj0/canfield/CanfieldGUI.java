package canfield;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.awt.event.MouseEvent;

/**
 * A top-level GUI for Canfield solitaire.
 * @author JuKyung Choi
 */
class CanfieldGUI extends TopLevel {

    /** A new window with given TITLE and displaying GAME. */
    CanfieldGUI(String title, Game game) {
        super(title, true);
        _game = game;
        addLabel("Canfield Solitaire GUI",
            new LayoutSpec("y", 0, "x", 0));
        addButton("Quit", "quit", new LayoutSpec("y", 0, "x", 1));
        addButton("Undo", "undoMove", new LayoutSpec("y", 1, "x", 1));
        _display = new GameDisplay(game);
        add(_display, new LayoutSpec("y", 2, "width", 2));
        _display.setMouseHandler("press", this, "mousePressed");
        _display.setMouseHandler("release", this, "mouseReleased");
        _display.setMouseHandler("drag", this, "mouseDragged");
        display(true);
    }

    /** Respond to "Quit" button. */
    public void quit(String dummy) {
        System.exit(1);
    }

    /**
     * Responds to "undoMove" button
     * DUMMY is a dummy string.
     */
    public void undoMove(String dummy) {
        _game.undoMove();
        for (int i = 1; i < 5; i++) {
            _display.setty(i - 1, STTABY + (_game.tableauSize(i) - 1) * INCR);
        }
        _display.repaint();
    }

    /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mousePressed(MouseEvent event) {
        _display.setClickedX(event.getX());
        _display.setClickedY(event.getY());
        if (_display.getClickedY() > 0) {
            pressed = _display.findCard(event.getX(), event.getY());
            pileTabNum = _display.findTabPile(_display.getClickedX(),
                    _display.getClickedY());
            pileFoundNum = _display.findFoundPile(_display.getClickedX(),
                    _display.getClickedY());
            if (pressed == _game.topStock()) {
                _game.stockToWaste();
                if (_game.stockEmpty()) {
                    _game.stockToWaste();
                }
            } else if (pressed == _game.topWaste()) {
                _display.wasteToFound(pressed);
            } else if (pressed == _game.topReserve()) {
                if (_game.topReserve() != null) {
                    _display.resToFound(pressed);
                }
            } else if (_display.findTabPile(_display.getClickedX(),
                    _display.getClickedY()) != -1) {
                _display.tabToFound1(pressed);
            }
        }
        _display.repaint();
    }

    /** Action in response to mouse-released event EVENT. */
    public synchronized void mouseReleased(MouseEvent event) {
        _display.settx(0, TX0);
        _display.settx(1, TX1);
        _display.setfx(0, TX0);
        _display.setfx(1, TX1);
        _display.setfx(2, TX2);
        _display.setfx(3, TX3);
        _display.settx(2, TX2);
        _display.settx(3, TX3);
        _display.setfy(0, FOUNDY);
        _display.setfy(1, FOUNDY);
        _display.setfy(2, FOUNDY);
        _display.setfy(3, FOUNDY);
        _display.setty(0, STTABY + INCR * (_game.tableauSize(1) - 1));
        _display.setty(1, STTABY + INCR * (_game.tableauSize(2) - 1));
        _display.setty(2, STTABY + INCR * (_game.tableauSize(3) - 1));
        _display.setty(3, STTABY + INCR * (_game.tableauSize(4) - 1));

        relX = event.getX();
        relY = event.getY();
        if (_display.getClickedY() > 0) {
            if (pressed == _game.topWaste()) {
                if (_display.getCounter() == 0) {
                    _display.wasteToTab(pressed, relX, relY);
                }
            } else if (pressed  == _game.topReserve()) {
                if (_display.getCounter() == 0) {
                    _display.resToTab(pressed, relX, relY);
                }
            }
            if (_display.getCounter() == 0) {
                if (pileFoundNum > 0 && _display.findTabPile(relX, relY) > 0) {
                    _display.foundToTab(_display.getClickedX(),
                            _display.getClickedY(), relX, relY);
                } else if (pileTabNum > 0 && _display.findTabPile(relX, relY)
                        > 0) {
                    _display.tabToTab(pressed, _display.getClickedX(),
                            _display.getClickedY(), relX, relY);
                }
            }

            for (int a = 0; a < 4; a++) {
                _display.settx(a, _display.gettArr()[a]);
                _display.setfx(a, _display.gettArr()[a]);
                _display.setfy(a , 4 * 5);
                _display.setClickedTY(a, _display.getty()[a]);
            }
        }
        _display.repaint();
        _display.setCounter(0);
    }

    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
        int dragX = event.getX();
        int dragY = event.getY();
        int prevX;
        int prevY;
        if (_display.getClickedY() > 0) {
            if (pileTabNum > 0) {
                prevX = _display.getClickedX()
                        - _display.gettArr()[pileTabNum - 1];
                prevY = _display.getClickedTY()[pileTabNum - 1]
                        - _display.getClickedY();
                _display.settx(pileTabNum - 1, dragX - prevX);
                _display.setty(pileTabNum - 1, dragY + prevY);
            }
            if (pileFoundNum > 0) {
                prevX = _display.getClickedX() - _display.getfArr()[pileFoundNum
                    - 1];
                prevY = _display.getClickedY() - 5 * 4;
                _display.setfx(pileFoundNum - 1, dragX - prevX);
                _display.setfy(pileFoundNum - 1, dragY - prevY);
            }
        }
        _display.repaint();
    }

    /** The board widget. */
    private final GameDisplay _display;
    /** The game I am consulting. */
    private final Game _game;
    /** Card pressed. */
    private Card pressed;
    /** Released x coord. */
    private static int relX;
    /** Released y coord. */
    private static int relY;
    /** Pile number of the tableau. */
    private static int pileTabNum;
    /** Distance between cards. */
    private static final int INCR = 30;
    /** Y coordinate for foundation. */
    private static final int FOUNDY = 20;
    /** Starting tableau y coordinate. */
    private static final int STTABY = 195;
    /** X coordinate of the first tableau pile. */
    private static final int TX0 = 300;
    /** X coordinate of the fourth tableau pile. */
    private static final int TX1 = 400;
    /** X coordinate of the third tableau pile. */
    private static final int TX2 = 500;
    /** X coordinate of the last tableau pile. */
    private static final int TX3 = 600;
    /**
     * Returns piletabnum.
     * @return pile number of the clicked tableau.
     */
    static int getPileTabNum() {
        return pileTabNum;
    }
    /** pile number of the foundation. */
    private static int pileFoundNum;
}
