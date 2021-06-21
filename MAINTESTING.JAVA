package game2048;

import ucb.util.CommandArgs;
import game2048.gui.Game;
import static game2048.Main.Side.*;

/** The main class for the 2048 game.
 *  @author Thomas Wu
 */
public class Main {

    /** Size of the board: number of rows and of columns. */
    static final int SIZE = 4;
    /** Number of squares on the board. */
    static final int SQUARES = SIZE * SIZE;
    /** The necessary score for victory. */
    static final int WINNING = 2048;

    /** Symbolic names for the four sides of a board. */
    static enum Side { NORTH, EAST, SOUTH, WEST };

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.); --testing (take random tiles and moves from
     *  standard input); and --no-display. */
    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log --testing --no-display",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log ] [ --testing ] [ --no-display ]");
            System.exit(1);
        }

        Main game = new Main(options);

        while (game.play()) {
            /* No action */
        }
        System.exit(0);
    }

    /** A new Main object using OPTIONS as options (as for main). */
    Main(CommandArgs options) {
        boolean log = options.contains("--log"),
            display = !options.contains("--no-display");
        long seed = !options.contains("--seed") ? 0 : options.getLong("--seed");
        _testing = options.contains("--testing");
        _game = new Game("2048", SIZE, seed, log, display, _testing);
    }

    /** Reset the score for the current game to 0 and clear the board. */
    void clear() {
        _score = 0;
        _count = 0;
        _game.clear();
        _game.setScore(_score, _maxScore);
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                _board[r][c] = 0;
            }
        }
    }

    /** Play one game of 2048, updating the maximum score. Return true
     *  iff play should continue with another game, or false to exit. */
    boolean play() {
        clear();
        setRandomPiece();
        _game.setScore(_score, _maxScore);


        while (true) {
            setRandomPiece();
            _game.displayMoves();

            if (gameOver()) {
                _maxScore = Math.max(_score, _maxScore);
                _game.setScore(_score, _maxScore);
                _game.endGame();
            }

        GetMove:
            while (true) {
                String key = _game.readKey();
                if (key.equals("/u2191")) {
                    key = "Up";
                }
                if (key.equals("/u2193")) {
                    key = "Down";
                }
                if (key.equals("/u2190")) {
                    key = "Left";
                }
                if (key.equals("/u2192")) {
                    key = "Right";
                }
                if (key == "→") {
                    key = "Right";
                }
                if (key == "←") {
                    key = "Left";
                }
                if (key == "↑") {
                    key = "Up";
                }
                if (key == "↓") {
                    key = "Down";
                }

                switch (key) {
                case "Up": case "Down": case "Left": case "Right":
                    if (!gameOver() && tiltBoard(keyToSide(key))) {
                        break GetMove;
                    }

                    break;
                case "New Game":
                    clear();
                    return true;
                case "Quit":
                    return false;
                default:
                    break;
                }
            }
            _game.displayMoves();
        }
    }


    /** Returns true if another move is possible by comparing
      * every value with its adjacent value to see if it is the
      * same value. if it's the same value, then a move (merge)
      * can definitely be made. Otherwise, return false */
    boolean canMove() {
        for (int x = 0; x < SIZE; x += 1) {
            for (int y = 0; y < SIZE - 1; y += 1) {
                int testy = y + 1;
                if (_board[x][y] == _board[x][testy]) {
                    return true;
                }
            }
        }

        for (int y2 = 0; y2 < SIZE; y2 += 1) {
            for (int x2 = 0; x2 < SIZE  - 1; x2 += 1) {
                int testx = x2 + 1;
                if (_board[x2][y2] == _board[testx][y2]) {
                    return true;
                }
            }
        }
        return false;

    }


    /** Return true iff the current game is over (no more moves
     *  possible). */
    boolean gameOver() {
        if (_count == SQUARES && !canMove() || _score == WINNING) {
            return true;
        }
        return false;
    }

    /** Add a tile to a random, empty position, choosing a value (2 or
     *  4) at random.  Has no effect if the board is currently full. */
    void setRandomPiece() {
        if (_count == SQUARES || gameOver()) {
            return;
        }
        int[] y = _game.getRandomTile();
        if (_board[y[1]][y[2]] == 0) {
            _game.addTile(y[0], y[1], y[2]);
            _board[y[1]][y[2]] = y[0];
            _count += 1;
        } else {
            setRandomPiece();
        }
    }

    /** Perform the result of tilting the board toward SIDE.
     *  Returns true iff the tilt changes the board. **/
    boolean tiltBoard(Side side) {
        /* As a suggestion (see the project text), you might try copying
         * the board to a local array, turning it so that edge SIDE faces
         * north.  That way, you can re-use the same logic for all
         * directions.  (As usual, you don't have to). */
        int[][] board = new int[SIZE][SIZE];
        int[][] board2 = new int[SIZE][SIZE];

        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                board[r][c] =
                    _board[tiltRow(side, r, c)][tiltCol(side, r, c)];

                board2[r][c] =
                    _board[tiltRow(NORTH, r, c)][tiltCol(NORTH, r, c)];
            }
        }
        int[][] shiftedBoard = moveUp(board, side);
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                _board[tiltRow(side, r, c)][tiltCol(side, r, c)]
                    = shiftedBoard[r][c];
            }
        }

        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                if (board2[r][c] != _board[r][c]) {
                    return true;
                }
            }
        }
        return false;
    }


    /** Moves or merges tiles up. Assumes north orientation.
      * @return The board move Up. BOARD is the board that is taken in
      * and it will be modified. SIDE is the orientation so that
      * we know what to return the untilted board as for the
      * movetile and mergetile functions.
      */
    public int[][] moveUp(int[][] board, Side side) {
        for (int x = 0; x < SIZE; x += 1) {
            for (int y = 0; y < SIZE; y += 1) {
                int[] nextValue = nextNumber(board, x, y);
                int row = nextValue[1];
                int[] nextValueAfter = nextNumber(board, row, y);
                int untiltedRow = tiltRow(side, x, y);
                int untiltedCol = tiltCol(side, x, y);
                int valueAtNew = board[x][y];
                if (board[x][y] == 0 && nextValue[0] == nextValueAfter[0]
                    && nextValue[0] != 0) {
                    board[row][y] = 0;
                    board[nextValueAfter[1]][y] = 0;
                    board[x][y] += 2 * nextValue[0];
                    _game.moveTile(nextValue[0], tiltRow(side, row, y),
                        tiltCol(side, row, y), untiltedRow, untiltedCol);
                    _game.mergeTile(nextValue[0], 2 * nextValue[0],
                        tiltRow(side, nextValueAfter[1], y),
                            tiltCol(side, nextValueAfter[1], y),
                                untiltedRow, untiltedCol);
                    _count -= 1;
                    _score += 2 * nextValue[0];
                    _game.setScore(_score, _maxScore);
                } else if (board[x][y] == nextValue[0] & nextValue[0] != 0) {
                    board[row][y] = 0;
                    board[x][y] += nextValue[0];
                    _game.mergeTile(nextValue[0], 2 * nextValue[0],
                        tiltRow(side, row, y), tiltCol(side, row, y),
                            untiltedRow, untiltedCol);
                    _count -= 1;
                    _score += 2 * nextValue[0];
                    _game.setScore(_score, _maxScore);
                } else if (board[x][y] == 0 && nextValue[0] != 0) {
                    board[row][y] = 0;
                    board[x][y] = nextValue[0];
                    _game.setScore(_score, _maxScore);
                    _game.moveTile(nextValue[0], tiltRow(side, row, y),
                        tiltCol(side, row, y), untiltedRow, untiltedCol);
                }

            }

        }
        return board;
    }

    /** Helper function for moveUp. Tells you the next number that
      * appears in the COLUMN. If we're at the end of the list,
      * then returns 0, chosen arbitrarily.
      * Takes in the BOARD, the column we're looking at, and
      * the first Index STARTINDEX we're looking at. This function aims to
      * find the first non-zero number AFTER the index, and return
      * it as an int with value. Also changes the value
      * at that point to 0, so we don't end up repeating.
      * If there's no more non-zero values, return 0.
      */
    public int[] nextNumber(int[][] board, int startIndex, int column) {
        /* If we're already at the bottom of the board. */
        if (startIndex == SIZE - 1) {
            return new int[]{0, startIndex};
        }
        for (int indx = startIndex + 1; indx < SIZE; indx += 1) {
            if (board[indx][column] != 0) {
                return new int[]{board[indx][column], indx};
            }
        }

        return new int[]{0, startIndex};
    }

    /** Return the row number on a playing board that corresponds to row R
     *  and column C of a board turned so that row 0 is in direction SIDE (as
     *  specified by the definitions of NORTH, EAST, etc.).  So, if SIDE
     *  is NORTH, then tiltRow simply returns R (since in that case, the
     *  board is not turned).  If SIDE is WEST, then column 0 of the tilted
     *  board corresponds to row SIZE - 1 of the untilted board, and
     *  tiltRow returns SIZE - 1 - C. */
    int tiltRow(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return r;
        case EAST:
            return c;
        case SOUTH:
            return SIZE - 1 - r;
        case WEST:
            return SIZE - 1 - c;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the column number on a playing board that corresponds to row
     *  R and column C of a board turned so that row 0 is in direction SIDE
     *  (as specified by the definitions of NORTH, EAST, etc.). So, if SIDE
     *  is NORTH, then tiltCol simply returns C (since in that case, the
     *  board is not turned).  If SIDE is WEST, then row 0 of the tilted
     *  board corresponds to column 0 of the untilted board, and tiltCol
     *  returns R. */
    int tiltCol(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return c;
        case EAST:
            return SIZE - 1 - r;
        case SOUTH:
            return SIZE - 1 - c;
        case WEST:
            return r;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the side indicated by KEY ("Up", "Down", "Left",
     *  or "Right"). */
    Side keyToSide(String key) {
        switch (key) {
        case "Up":
            return NORTH;
        case "Down":
            return SOUTH;
        case "Left":
            return WEST;
        case "Right":
            return EAST;
        default:
            throw new IllegalArgumentException("unknown key designation");
        }
    }


    /** Represents the board: _board[r][c] is the tile value at row R,
     *  column C, or 0 if there is no tile there. */
    private final int[][] _board = new int[SIZE][SIZE];

    /** True iff --testing option selected. */
    private boolean _testing;
    /** THe current input source and output sink. */
    private Game _game;
    /** The score of the current game, and the maximum final score
     *  over all games in this session. */
    private int _score, _maxScore;
    /** Number of tiles on the board. */
    private int _count;
}
