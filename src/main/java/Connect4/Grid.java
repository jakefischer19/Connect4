package Connect4;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

// Grid for the game board
public class Grid {

    JFrame frame;
    MultiDraw gameBoard;
    private JLabel messageLabel = new JLabel("");
    private Color color;
    private Color opponentColor;
    Color red = new Color(255, 0, 0);
    Color yellow = new Color(255, 255, 0);
    static char mark;

    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private static PrintWriter out;

    public Grid(String serverAddress) throws Exception {

        // Setup Networking
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        frame = new JFrame("Connect 4");
        frame.setSize(750, 660);
        gameBoard = new MultiDraw(frame.getSize());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(frame.getSize());
        frame.add(gameBoard);
        frame.getContentPane().add(messageLabel, "South");
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws Exception {
        Grid client = new Grid("localhost");
        client.play();
//        if (!client.wantsToPlayAgain()) {
//            break;
//        }
    }

    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                mark = response.charAt(8);
                color = (mark == '1' ? red : yellow);
                opponentColor = (mark == '1' ? yellow : red);
                frame.setTitle("Connect 4 - Player " + mark);
            }
            while (true) {
                response = in.readLine();
                System.out.println(response);
                if (response.startsWith("VALID_MOVE")) {
                    int x = Integer.parseInt(response.substring(11, 12));
                    int y = Integer.parseInt(response.substring(12));
                    gameBoard.grid[y][x] = this.color;
                    gameBoard.repaint();
                    messageLabel.setText("Valid move, please wait");
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int x = Integer.parseInt(response.substring(15, 16));
                    int y = Integer.parseInt(response.substring(16));
                    gameBoard.grid[y][x] = opponentColor;
                    gameBoard.repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
        } finally {
            socket.close();
        }
    }

    public static class MultiDraw extends JPanel implements MouseListener {

        int startX = 10;
        int startY = 10;
        int cellDiameter = 100;
        int turn = 2;
        int rows = 6;
        int cols = 7;

        Color[][] grid = new Color[rows][cols];

        public MultiDraw(Dimension dimension) {
            setSize(dimension);
            setPreferredSize(dimension);
            addMouseListener(this);
            //1. initialize array here
            int x = 0;
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[0].length; col++) {
                    grid[row][col] = new Color(255, 255, 255);
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Dimension d = getSize();
            g2.setColor(new Color(52, 53, 64));
            g2.fillRect(0, 0, d.width, d.height);
            startX = 0;
            startY = 0;

            //2) draw grid here
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[0].length; col++) {
                    g2.setColor(grid[row][col]);
                    g2.fillOval(startX, startY, cellDiameter, cellDiameter);
                    startX += cellDiameter;
                }
                startX = 0;
                startY += cellDiameter;
            }
            g2.setColor(new Color(255, 255, 255));
        }

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellX = x / cellDiameter;
            int cellY = y / cellDiameter;

            cellY = checkForOpenCell(cellX);

            out.println("MOVE " + cellX + cellY);

//            if (mark == '1') {
//                grid[cellY][cellX] = Color.red;
//            } else {
//                grid[cellY][cellX] = Color.yellow;
//            }
//            repaint();
        }

        public int checkForOpenCell(int cellX) {
            int cellY = rows - 1;

            while (!(grid[cellY][cellX].equals(new Color(255, 255, 255)) || cellY < 0)) {
                cellY--;
            }
            return cellY;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }
}
