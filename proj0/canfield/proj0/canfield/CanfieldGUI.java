package canfield;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.awt.event.MouseEvent;

/**
 * A top-level GUI for Canfield solitaire.
 * 
 * @author
 */
class CanfieldGUI extends TopLevel {

    /** A new window with given TITLE and displaying GAME. */
    CanfieldGUI(String title, Game game) {
        super(title, true);
        _game = game;
        addLabel("Canfield Solitaire GUI", new LayoutSpec("y", 0, "x", 0));
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
     */
    public void undoMove(String dummy) {
        _game.undoMove();
        for (int i = 1; i < 5; i++) {
            _display.ty[i - 1] = _display.tyN[i - 1] = 195 + (_game.tableauSize(i) - 1) * _display.incr;
        }
        _display.repaint();
    }

    /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mousePressed(MouseEvent event) {
        _display.clickedX = event.getX();
        _display.clickedY = event.getY();
        if (_display.clickedY > 0) {
            pressed = _display.findCard(event.getX(), event.getY());
            pileTabNum = _display.findTabPile(_display.clickedX, _display.clickedY);
            pileFoundNum = _display.findFoundPile(_display.clickedX, _display.clickedY);
            if (pressed == _game.topStock()) {
                _game.stockToWaste();
                if (_game.stockEmpty() == true) {
                    _game.stockToWaste();
                }
            } else if (pressed == _game.topWaste()) {
                _display.wasteToFound(pressed);
                if (_display.counter==0) {
                    _display.wasteToTab(pressed);
                }
            } else if (pressed == _game.topReserve()) {
                if (_game.topReserve() != null) {
                    _display.resToFound(pressed);
                    if (_display.counter==0) {
                        _display.resToTab(pressed);
                    }
                }
            } else if (_display.findTabPile(_display.clickedX, _display.clickedY) != -1) {
                _display.tabToFound1(pressed);
            }
        }
        _display.repaint();
    }

    /** Action in response to mouse-released event EVENT. */
    public synchronized void mouseReleased(MouseEvent event) {
        _display.tx[0] = _display.fx[0] = 300;
        _display.tx[1] = _display.fx[1] = 400;
        _display.tx[2] = _display.fx[2] = 500;
        _display.tx[3] = _display.fx[3] = 600;
        _display.fy[0] = _display.fy[1] = _display.fy[2] = _display.fy[3] = 20;

        relX = event.getX();
        relY = event.getY();
        if (_display.clickedY > 0) {

            if (_display.counter == 0) {
                if (pileFoundNum > 0 && _display.findTabPile(relX, relY) > 0) {
                    _display.foundToTab(_display.clickedX, _display.clickedY, relX, relY);
                } else if (pileTabNum > 0 && _display.findTabPile(relX, relY) > 0) {
                    _display.tabToTab(pressed, _display.clickedX, _display.clickedY, relX, relY);
                }
            }

            for (int a = 0; a < 4; a++) {
                _display.ty[a] = _display.tyN[a];
                _display.tx[a] = _display.fx[a] = _display.tArr[a];
                _display.fy[a] = 20;
                _display.clickedTY[a] = _display.ty[a];
            }
        }

        _display.repaint();
        _display.counter = 0;
    }

    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
    	int dragX = event.getX();
        int dragY = event.getY();
        int prevX;
        int prevY;
        if (_display.clickedY > 0) {
            if (pileTabNum > 0) {
                prevX = _display.clickedX - _display.tArr[pileTabNum - 1];
                prevY = _display.clickedTY[pileTabNum - 1] - _display.clickedY;
                _display.tx[pileTabNum - 1] = dragX - prevX;
                _display.ty[pileTabNum - 1] = dragY + prevY;
            }
            if (pileFoundNum > 0) {
                prevX = _display.clickedX - _display.fArr[pileFoundNum - 1];
                prevY = _display.clickedY - 20;
                _display.fx[pileFoundNum - 1] = dragX - prevX;
                _display.fy[pileFoundNum - 1] = dragY - prevY;
            }
        }
        _display.repaint();
    }

    /** The board widget. */
    private final GameDisplay _display;

    /** The game I am consulting. */
    private final Game _game;

    /** card x coordinate. */
    public int cx;
    /** card y coordinate. */
    public int cy;
    /** card pressed. */
    public Card pressed;
    /** released x coord */
    public static int relX;
    /** releeased y coord. */
    public static int relY;
    /** pile number of the tableau.*/
    public static int pileTabNum;
    /** pile number of the foundation. */
    public static int pileFoundNum;
}
