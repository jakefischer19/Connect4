package Connect4;

import java.awt.Color;
import java.net.ServerSocket;
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
                Game.Player player1 = game.new Player(listener.accept(), '1', new Color(255, 0, 0));
                Game.Player player2 = game.new Player(listener.accept(), '2', new Color(255, 255, 0));
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
    /**
     * The current player.
     */
    Player currentPlayer;

//    public boolean checkForWinner(int x, int y) {
//        
//    }
    /**
     * Returns whether there are no more empty squares.
     */
    public boolean boardFull() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == new Color(255, 255, 255)) {
                    return false;
                }
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
        System.out.println(board[0][x]);
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

        /**
         * Accepts notification of who the opponent is.
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Handles the otherPlayerMoved message.
         */
        public void otherPlayerMoved(int x, int y) {
            output.println("OPPONENT_MOVED " + x + y);
            //output.println(checkForWinner(x, y) ? "DEFEAT" : boardFull() ? "TIE" : "");
        }

        /**
         * The run method of this thread.
         */
        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected");

                // Tell the first player that it is her turn.
                if (mark == '1') {
                    output.println("MESSAGE Your move");
                }

                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    System.out.println(command);
                    if (command.startsWith("MOVE")) {
                        int x = Integer.parseInt(command.substring(5, 6));
                        int y = Integer.parseInt(command.substring(6));
                        if (isValidMove(x, y, this)) {
                            output.println("VALID_MOVE " + x + y);
                            //output.println(checkForWinner(x, y) ? "VICTORY" : boardFull() ? "TIE" : "");
                        } else {
                            output.println("MESSAGE ?");
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
