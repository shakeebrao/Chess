import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chess Game with Swing GUI
 * A fully functional 2D chess board implementation
 */
public class ChessGameSwing extends JFrame {
    
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    
    private GameBoard gameBoard;
    private JPanel[][] tiles = new JPanel[BOARD_SIZE][BOARD_SIZE];
    private JLabel[][] pieceLabels = new JLabel[BOARD_SIZE][BOARD_SIZE];
    private JLabel statusLabel;
    private JLabel turnLabel;
    
    // Selection state
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean pieceSelected = false;
    private List<int[]> validMoveSquares = new ArrayList<>();
    
    // Colors
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(255, 255, 0, 150);
    private static final Color VALID_MOVE_COLOR = new Color(0, 255, 0, 100);
    private static final Color CAPTURE_COLOR = new Color(255, 0, 0, 100);
    private static final Color CHECK_COLOR = new Color(255, 50, 50, 180);
    
    public ChessGameSwing() {
        gameBoard = new GameBoard();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(44, 44, 44));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Chess board panel
        JPanel boardPanel = createBoardPanel();
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.EAST);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        
        updateBoard();
    }
    
    private JPanel createBoardPanel() {
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBackground(new Color(44, 44, 44));
        
        // Create the main board
        JPanel board = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        board.setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        board.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 3));
        
        // Create tiles (row 7 at top, row 0 at bottom for white's perspective)
        for (int displayRow = 0; displayRow < BOARD_SIZE; displayRow++) {
            int row = 7 - displayRow; // Convert display row to board row
            for (int col = 0; col < BOARD_SIZE; col++) {
                JPanel tile = createTile(row, col);
                tiles[row][col] = tile;
                board.add(tile);
            }
        }
        
        // Row labels (numbers)
        JPanel leftLabels = new JPanel(new GridLayout(BOARD_SIZE, 1));
        leftLabels.setBackground(new Color(44, 44, 44));
        for (int i = BOARD_SIZE; i >= 1; i--) {
            JLabel label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setPreferredSize(new Dimension(25, TILE_SIZE));
            leftLabels.add(label);
        }
        
        // Column labels (letters)
        JPanel bottomLabels = new JPanel(new GridLayout(1, BOARD_SIZE));
        bottomLabels.setBackground(new Color(44, 44, 44));
        for (int i = 0; i < BOARD_SIZE; i++) {
            JLabel label = new JLabel(String.valueOf((char) ('a' + i)), SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setPreferredSize(new Dimension(TILE_SIZE, 25));
            bottomLabels.add(label);
        }
        
        boardContainer.add(leftLabels, BorderLayout.WEST);
        boardContainer.add(board, BorderLayout.CENTER);
        boardContainer.add(bottomLabels, BorderLayout.SOUTH);
        
        // Spacer for top-left corner
        JPanel corner = new JPanel();
        corner.setPreferredSize(new Dimension(25, 25));
        corner.setBackground(new Color(44, 44, 44));
        
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(new Color(44, 44, 44));
        topRow.add(corner, BorderLayout.WEST);
        boardContainer.add(topRow, BorderLayout.NORTH);
        
        return boardContainer;
    }
    
    private JPanel createTile(int row, int col) {
        JPanel tile = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Draw base color
                boolean isLight = (row + col) % 2 == 0;
                g2d.setColor(isLight ? LIGHT_SQUARE : DARK_SQUARE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw selection/highlight overlay
                if (pieceSelected && row == selectedRow && col == selectedCol) {
                    g2d.setColor(SELECTED_COLOR);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else if (isValidMoveSquare(row, col)) {
                    Piece target = gameBoard.getPiece(row, col);
                    g2d.setColor(target != null ? CAPTURE_COLOR : VALID_MOVE_COLOR);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                
                // Highlight king if in check
                Piece piece = gameBoard.getPiece(row, col);
                if (piece != null && piece.getType() == Piece.Type.KING) {
                    if (gameBoard.isInCheck(piece.getColor())) {
                        g2d.setColor(CHECK_COLOR);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            }
        };
        
        tile.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        
        // Piece label
        JLabel pieceLabel = new JLabel("", SwingConstants.CENTER);
        pieceLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 56));
        pieceLabels[row][col] = pieceLabel;
        tile.add(pieceLabel, BorderLayout.CENTER);
        
        // Mouse listener
        final int r = row;
        final int c = col;
        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTileClick(r, c);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBorder(null);
            }
        });
        
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return tile;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(60, 60, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(200, 0));
        
        // Title
        JLabel title = new JLabel("CHESS");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Turn indicator
        turnLabel = new JLabel("White's Turn");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        turnLabel.setForeground(new Color(144, 238, 144));
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(turnLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Status label
        statusLabel = new JLabel("<html><center>Click a piece<br>to select it</center></html>");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Legend
        JLabel legendTitle = new JLabel("Pieces:");
        legendTitle.setFont(new Font("Arial", Font.BOLD, 16));
        legendTitle.setForeground(Color.WHITE);
        legendTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(legendTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[][] pieces = {
            {"♔", "King"}, {"♕", "Queen"}, {"♖", "Rook"},
            {"♗", "Bishop"}, {"♘", "Knight"}, {"♙", "Pawn"}
        };
        
        for (String[] piece : pieces) {
            JLabel pieceLabel = new JLabel(piece[0] + " - " + piece[1]);
            pieceLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
            pieceLabel.setForeground(Color.LIGHT_GRAY);
            pieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(pieceLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        panel.add(Box.createVerticalGlue());
        
        // New Game button
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 14));
        newGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameBtn.addActionListener(e -> resetGame());
        newGameBtn.setFocusPainted(false);
        newGameBtn.setBackground(new Color(70, 130, 180));
        newGameBtn.setForeground(Color.WHITE);
        newGameBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        newGameBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        panel.add(newGameBtn);
        
        return panel;
    }
    
    private void handleTileClick(int row, int col) {
        if (!pieceSelected) {
            // Select a piece
            Piece piece = gameBoard.getPiece(row, col);
            if (piece != null && piece.getColor().equals(gameBoard.getCurrentTurn())) {
                selectedRow = row;
                selectedCol = col;
                pieceSelected = true;
                calculateValidMoves(row, col);
                statusLabel.setText("<html><center>Selected " + piece.getType() + 
                                   "<br>at " + toChessNotation(row, col) + "</center></html>");
                repaintBoard();
            } else if (piece != null) {
                statusLabel.setText("<html><center>Not your turn!<br>" + 
                                   capitalize(gameBoard.getCurrentTurn()) + "'s turn</center></html>");
            } else {
                statusLabel.setText("<html><center>Empty square</center></html>");
            }
        } else {
            // Try to move the selected piece
            if (row == selectedRow && col == selectedCol) {
                // Deselect
                clearSelection();
                statusLabel.setText("<html><center>Piece deselected</center></html>");
            } else {
                // Attempt move
                boolean success = gameBoard.movePiece(selectedRow, selectedCol, row, col);
                if (success) {
                    clearSelection();
                    updateBoard();
                    
                    String opponent = gameBoard.getCurrentTurn();
                    turnLabel.setText(capitalize(opponent) + "'s Turn");
                    turnLabel.setForeground(opponent.equals("white") ? 
                                           new Color(144, 238, 144) : new Color(255, 182, 193));
                    
                    // Check for check/checkmate/stalemate
                    if (gameBoard.isInCheck(opponent)) {
                        if (gameBoard.isCheckmate(opponent)) {
                            String winner = opponent.equals("white") ? "Black" : "White";
                            statusLabel.setText("<html><center>CHECKMATE!<br>" + winner + " wins!</center></html>");
                            showGameOverDialog(winner + " wins by checkmate!");
                        } else {
                            statusLabel.setText("<html><center>" + capitalize(opponent) + 
                                               "<br>is in CHECK!</center></html>");
                        }
                    } else if (gameBoard.isStalemate(opponent)) {
                        statusLabel.setText("<html><center>STALEMATE!<br>Game is a draw</center></html>");
                        showGameOverDialog("Stalemate! The game is a draw.");
                    } else {
                        statusLabel.setText("<html><center>Move successful</center></html>");
                    }
                } else {
                    statusLabel.setText("<html><center>Invalid move!<br>Try again</center></html>");
                }
            }
        }
    }
    
    private void calculateValidMoves(int fromRow, int fromCol) {
        validMoveSquares.clear();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (gameBoard.isValidMove(fromRow, fromCol, r, c)) {
                    validMoveSquares.add(new int[]{r, c});
                }
            }
        }
    }
    
    private boolean isValidMoveSquare(int row, int col) {
        for (int[] square : validMoveSquares) {
            if (square[0] == row && square[1] == col) {
                return true;
            }
        }
        return false;
    }
    
    private void clearSelection() {
        pieceSelected = false;
        selectedRow = -1;
        selectedCol = -1;
        validMoveSquares.clear();
        repaintBoard();
    }
    
    private void updateBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = gameBoard.getPiece(row, col);
                JLabel label = pieceLabels[row][col];
                
                if (piece != null) {
                    label.setText(piece.getSymbol());
                    label.setForeground(piece.getColor().equals("white") ? 
                                       new Color(255, 255, 255) : new Color(30, 30, 30));
                } else {
                    label.setText("");
                }
            }
        }
        repaintBoard();
    }
    
    private void repaintBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col].repaint();
            }
        }
    }
    
    private void resetGame() {
        gameBoard = new GameBoard();
        clearSelection();
        turnLabel.setText("White's Turn");
        turnLabel.setForeground(new Color(144, 238, 144));
        statusLabel.setText("<html><center>Click a piece<br>to select it</center></html>");
        updateBoard();
    }
    
    private void showGameOverDialog(String message) {
        int option = JOptionPane.showConfirmDialog(
            this,
            message + "\n\nWould you like to play again?",
            "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        }
    }
    
    private String toChessNotation(int row, int col) {
        return "" + (char) ('a' + col) + (row + 1);
    }
    
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            ChessGameSwing game = new ChessGameSwing();
            game.setVisible(true);
        });
    }
}
