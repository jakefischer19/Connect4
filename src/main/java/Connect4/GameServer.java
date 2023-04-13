package Connect4;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {

    private static final int PORT = 8901;

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Connect 4 Server is Running");
        try {
            while (true) {
                Game game = new Game();
                Game.Player player1 = game.new Player(listener.accept(),
                        '1', new Color(255, 0, 0));
                Game.Player player2 = game.new Player(listener.accept(),
                        '2', new Color(255, 255, 0));
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                game.currentPlayer = player1;
                player1.start();
                player2.start();
            }
        } finally {
            listener.close();
        }
    }
}

class Game {

    int rows = 6;
    int cols = 7;

    private Color[][] board = new Color[rows][cols];

    Player currentPlayer;

    /**
     * Called when a player makes a move. Checks to see if the move results in a
     * win. Takes in the row and column indexes of a move and tests for 4 pieces
     * in a row on vertical, horizontal, ascending diagonal, and descending
     * diagonal.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean checkForWinner(int x, int y) {
        int count = 0;
        Color curColor = currentPlayer.opponent.color;
        // Horizontal win check
        for (int i = 0; i < cols; i++) {
            if (board[y][i] == curColor) {
                count++;
            } else {
                count = 0;
            }

            if (count == 4) {
                return true;
            }
        }
        count = 0;

        // Vertical win check
        for (int i = 0; i < rows; i++) {
            if (board[i][x] == curColor) {
                count++;
            } else {
                count = 0;
            }

            if (count == 4) {
                return true;
            }
        }
        count = 0;

        // Diagonal win Check
        // Ascending Diagonal
        int row = y;
        int col = x;

        // Down and left
        while (board[row][col] == curColor) {
            count++;
            if (count == 4) {
                return true;
            }
            if (row == rows - 1 || col == 0) {
                break;
            }
            row++;
            col--;
        }

        // Up and right
        if (x != cols - 1 && y != 0) {
            row = y - 1;
            col = x + 1;
            while (board[row][col] == curColor) {
                count++;
                if (count == 4) {
                    return true;
                }
                if (row == 0 || col == cols - 1) {
                    break;
                }
                row--;
                col++;
            }
        }
        count = 0;

        // Descending Diagonal
        // Down and right
        row = y;
        col = x;

        while (board[row][col] == curColor) {
            count++;
            if (count == 4) {
                return true;
            }
            if (row == rows - 1 || col == cols - 1) {
                break;
            }
            row++;
            col++;
        }

        // Up and left
        if (x != 0 && y != 0) {
            row = y - 1;
            col = x - 1;
            while (board[row][col] == curColor) {
                count++;
                if (count == 4) {
                    return true;
                }
                if (row == 0 || col == 0) {
                    break;
                }
                row--;
                col--;
            }
        }

        return false;
    }

    /**
     * Checks whether or not there are any available cells for a player to make
     * a move
     */
    public boolean boardFull() {
        for (int col = 0; col < board[0].length; col++) {
            if (board[0][col] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a players move is valid or not. If the column that the
     * player clicks contains any blank spots it returns true. If that column is
     * full, returns false
     */
    public synchronized boolean isValidMove(int x, int y, Player player) {
        if (player == currentPlayer && board[0][x] == null) {
            board[y][x] = currentPlayer.color;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(x, y);
            return true;
        }
        return false;
    }

    class Player extends Thread {

        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        Color color;

        /**
         * Constructs a handler thread for a given socket and mark initializes
         * the stream fields, displays the first two welcoming messages.
         */
        public Player(Socket socket, char mark, Color color) {
            this.socket = socket;
            this.mark = mark;
            this.color = color;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        // Used to set the opponent of each player.
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Called when a player makes a move. A message containing the row and
         * column indexes of where the current player placed a piece is sent to
         * the opposing player for processing.
         *
         * @param x
         * @param y
         */
        public void otherPlayerMoved(int x, int y) {
            output.println("OPPONENT_MOVED " + x + y);
            output.println(checkForWinner(x, y) ? "DEFEAT" : boardFull() ? "TIE" : "");
        }

        /**
         * The run method of this thread.
         */
        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected");

                // Tell the first player that it is their turn.
                if (mark == '1') {
                    output.println("MESSAGE Your move");
                }

                /**
                 * Loop that constantly reads messages from clients and
                 * processes them. The x and y values are extracted from MOVE
                 * messages and passed into the checkForWinner function.
                 */
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int x = Integer.parseInt(command.substring(5, 6));
                        int y = Integer.parseInt(command.substring(6));
                        if (isValidMove(x, y, this)) {
                            output.println("VALID_MOVE " + x + y);
                            output.println(checkForWinner(x, y) ? "VICTORY" : boardFull() ? "TIE" : "");
                        } else {
                            output.println("MESSAGE Please wait for your turn.");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
