package Connect4;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public class GameClient {

    JFrame frame;
    JPanel board;
    JPanel panel;
    DrawGameBoard drawBoard;
    private JLabel messageLabel = new JLabel("");
    private Color color;
    private Color opponentColor;
    private JLabel title = new JLabel("");
    private JLabel four = new JLabel("");
    Color red = new Color(255, 0, 0);
    Color yellow = new Color(255, 255, 0);
    static char mark;

    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private static PrintWriter out;

    public GameClient(String serverAddress) throws Exception {

        // Setup Networking
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Build game client window
        frame = new JFrame("Connect 4");
        frame.setSize(740, 800);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(frame.getSize());
        frame.setResizable(false);

        panel = new JPanel();
        panel.setBackground(new Color(52, 53, 64));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // changed layout manager

        title.setOpaque(true);
        title.setFont(new java.awt.Font("Franklin Gothic Heavy", 1, 75));
        title.setForeground(new Color(255, 255, 255));
        title.setBackground(new Color(52, 53, 64));
        title.setText("Connect");
        title.setToolTipText("");

        four.setFont(new java.awt.Font("Franklin Gothic Heavy", 1, 94));
        four.setForeground(new Color(204, 0, 51));
        four.setBackground(new Color(52, 53, 64));
        four.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        four.setText("4");
        four.setToolTipText("");

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS)); // horizontal layout
        titlePanel.setOpaque(false); // make background transparent
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(20, 0))); // add spacing between title and four
        titlePanel.add(four);

        panel.add(titlePanel); // add nested panel to main panel
        panel.add(Box.createRigidArea(new Dimension(0, 5))); // added spacing between title and board

        frame.add(panel);

        drawBoard = new DrawGameBoard(frame.getSize());

        board = new JPanel();
        board.setBorder(BorderFactory.createEmptyBorder(0, 38, 0, 0));
        board.setBackground(new Color(52, 53, 64));
        board.add(drawBoard);
        panel.add(board);

        // Set up info label for turn and win conditions
        JPanel info = new JPanel();
        info.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 5));
        info.setBackground(new Color(52, 53, 64));
        messageLabel.setFont(new Font("Segui UI", Font.BOLD, 24));
        messageLabel.setForeground(Color.WHITE);
        info.add(messageLabel, BorderLayout.SOUTH);

        frame.getContentPane().add(info, "South");
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws Exception {
        String serverAddress;
    
        // Check if args[0] is provided
        if (args.length > 0) {
            serverAddress = args[0];
        } else {
            // Prompt the user for the server address if not provided
            serverAddress = JOptionPane.showInputDialog(
                null,
                "Enter the server address ('localhost' if running locally):",
                "Connect 4 - Server Address",
                JOptionPane.QUESTION_MESSAGE
            );
    
            // Exit if the user cancels the input dialog
            if (serverAddress == null || serverAddress.isEmpty()) {
                System.out.println("No server address provided. Exiting...");
                return;
            }
        }
    
        // Main game loop
        while (true) {
            GameClient client = new GameClient(serverAddress);
            client.play();
            if (!client.wantsToPlayAgain()) {
                break;
            }
        }
    }

    /**
     * The main client game loop. Each client will be assigned a mark and a
     * color upon connection to the server. The client continually loops and
     * listens for messages from the server.
     */
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
                if (response.startsWith("VALID_MOVE")) {
                    int x = Integer.parseInt(response.substring(11, 12));
                    int y = Integer.parseInt(response.substring(12));
                    drawBoard.grid[y][x] = this.color;
                    drawBoard.repaint();
                    messageLabel.setText("Valid move, wait for opponent");
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int x = Integer.parseInt(response.substring(15, 16));
                    int y = Integer.parseInt(response.substring(16));
                    drawBoard.grid[y][x] = opponentColor;
                    drawBoard.repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    messageLabel.setForeground(Color.green);
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    messageLabel.setForeground(Color.red);
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

    /**
     * Creates dialog window that confirms whether or not each player wants to
     * play a new game.
     */
    private boolean wantsToPlayAgain() {
        UIManager.put("OptionPane.background", new ColorUIResource(52, 53, 64));
        UIManager.put("Panel.background", new ColorUIResource(52, 53, 64));
        UIManager.put("OptionPane.messageForeground", Color.white);
        int response = JOptionPane.showConfirmDialog(frame, "Want to play again?",
                "New Game?", JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    // Class that handles the creation of the game grid.
    public static class DrawGameBoard extends JPanel implements MouseListener {

        int startX = 10;
        int startY = 10;
        int cellDiameter = 100;
        int turn = 2;
        int rows = 6;
        int cols = 7;

        Color[][] grid = new Color[rows][cols];

        public DrawGameBoard(Dimension dimension) {
            setSize(dimension);
            setPreferredSize(dimension);
            addMouseListener(this);

            // Initialize the grid with the default color of white.
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

            // Draws the game board on for the client
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[0].length; col++) {
                    g2.setColor(grid[row][col]);
                    g2.fillOval(startX, startY, cellDiameter, cellDiameter);
                    startX += cellDiameter;
                }
                startX = 0;
                startY += cellDiameter;
            }
        }

        /**
         * Gets the x and y position of a players mouse click. The x and y are
         * divided by the diameter of a board cell to get the row and column
         * that the player clicked in. The final row and column indexes are sent
         * to the server with a MOVE message.
         */
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cellX = x / cellDiameter;
            int cellY = y / cellDiameter;

            cellY = checkForOpenCell(cellX);

            out.println("MOVE " + cellX + cellY);
        }

        /**
         * Checks the column in which a player clicked for the first non
         * occupied cell.
         *
         * @param cellX
         * @return row index in which the next piece should be placed
         */
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
