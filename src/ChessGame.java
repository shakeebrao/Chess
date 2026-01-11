import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChessGame extends Application {
    
    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    
    private GameBoard gameBoard;
    private StackPane[][] tiles = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private Label statusLabel;
    private Label turnLabel;
    
    // Selection state
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean pieceSelected = false;
    
    @Override
    public void start(Stage primaryStage) {
        gameBoard = new GameBoard();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2c2c2c;");
        
        // Create chess board
        GridPane chessBoard = createChessBoard();
        
        // Create info panel
        VBox infoPanel = createInfoPanel();
        
        root.setCenter(chessBoard);
        root.setRight(infoPanel);
        BorderPane.setMargin(chessBoard, new Insets(20));
        BorderPane.setMargin(infoPanel, new Insets(20));
        
        Scene scene = new Scene(root, TILE_SIZE * BOARD_SIZE + 250, TILE_SIZE * BOARD_SIZE + 60);
        primaryStage.setTitle("Chess - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        updateBoard();
    }
    
    private GridPane createChessBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = createTile(row, col);
                tiles[row][col] = tile;
                // Chess board: row 0 is bottom (white's first rank)
                // Display: row 7 at top, row 0 at bottom
                grid.add(tile, col, 7 - row);
            }
        }
        
        // Add row labels (1-8)
        for (int row = 0; row < BOARD_SIZE; row++) {
            Label rowLabel = new Label(String.valueOf(row + 1));
            rowLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            rowLabel.setTextFill(Color.WHITE);
            rowLabel.setPrefSize(20, TILE_SIZE);
            rowLabel.setAlignment(Pos.CENTER);
            grid.add(rowLabel, -1, 7 - row);
        }
        
        // Add column labels (a-h)
        for (int col = 0; col < BOARD_SIZE; col++) {
            Label colLabel = new Label(String.valueOf((char) ('a' + col)));
            colLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            colLabel.setTextFill(Color.WHITE);
            colLabel.setPrefSize(TILE_SIZE, 20);
            colLabel.setAlignment(Pos.CENTER);
            grid.add(colLabel, col, 8);
        }
        
        return grid;
    }
    
    private StackPane createTile(int row, int col) {
        StackPane tile = new StackPane();
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        
        Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
        boolean isLight = (row + col) % 2 == 0;
        rect.setFill(isLight ? Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99));
        
        tile.getChildren().add(rect);
        
        final int r = row;
        final int c = col;
        
        tile.setOnMouseClicked(e -> handleTileClick(r, c));
        
        tile.setOnMouseEntered(e -> {
            if (!pieceSelected || (r == selectedRow && c == selectedCol)) {
                rect.setStroke(Color.YELLOW);
                rect.setStrokeWidth(3);
            }
        });
        
        tile.setOnMouseExited(e -> {
            if (!(pieceSelected && r == selectedRow && c == selectedCol)) {
                rect.setStroke(null);
            }
        });
        
        return tile;
    }
    
    private VBox createInfoPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 10;");
        panel.setPrefWidth(200);
        
        Label title = new Label("CHESS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        
        turnLabel = new Label("White's Turn");
        turnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        turnLabel.setTextFill(Color.LIGHTGREEN);
        
        statusLabel = new Label("Select a piece to move");
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setWrapText(true);
        
        // Legend
        VBox legend = new VBox(10);
        legend.setAlignment(Pos.CENTER_LEFT);
        Label legendTitle = new Label("Pieces:");
        legendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        legendTitle.setTextFill(Color.WHITE);
        
        String[] pieceNames = {"K - King", "Q - Queen", "R - Rook", "B - Bishop", "N - Knight", "P - Pawn"};
        legend.getChildren().add(legendTitle);
        for (String name : pieceNames) {
            Label pieceLabel = new Label(name);
            pieceLabel.setFont(Font.font("Arial", 12));
            pieceLabel.setTextFill(Color.LIGHTGRAY);
            legend.getChildren().add(pieceLabel);
        }
        
        panel.getChildren().addAll(title, turnLabel, statusLabel, legend);
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
                highlightSelectedTile(row, col);
                highlightValidMoves(row, col);
                statusLabel.setText("Selected " + piece.getType() + " at " + toChessNotation(row, col));
            } else if (piece != null) {
                statusLabel.setText("Not your turn! It's " + gameBoard.getCurrentTurn() + "'s turn.");
            } else {
                statusLabel.setText("No piece at this square");
            }
        } else {
            // Try to move the selected piece
            if (row == selectedRow && col == selectedCol) {
                // Deselect
                clearSelection();
                statusLabel.setText("Piece deselected");
            } else {
                // Attempt move
                boolean success = gameBoard.movePiece(selectedRow, selectedCol, row, col);
                if (success) {
                    updateBoard();
                    clearSelection();
                    
                    String opponent = gameBoard.getCurrentTurn();
                    turnLabel.setText(capitalize(opponent) + "'s Turn");
                    
                    // Check for check/checkmate
                    if (gameBoard.isInCheck(opponent)) {
                        if (gameBoard.isCheckmate(opponent)) {
                            String winner = opponent.equals("white") ? "Black" : "White";
                            statusLabel.setText("CHECKMATE! " + winner + " wins!");
                            showAlert("Checkmate!", winner + " wins the game!");
                        } else {
                            statusLabel.setText(capitalize(opponent) + " is in CHECK!");
                        }
                    } else if (gameBoard.isStalemate(opponent)) {
                        statusLabel.setText("STALEMATE! Game is a draw.");
                        showAlert("Stalemate!", "The game is a draw.");
                    } else {
                        statusLabel.setText("Move successful");
                    }
                } else {
                    statusLabel.setText("Invalid move! Try again.");
                }
            }
        }
    }
    
    private void highlightSelectedTile(int row, int col) {
        Rectangle rect = (Rectangle) tiles[row][col].getChildren().get(0);
        rect.setStroke(Color.YELLOW);
        rect.setStrokeWidth(3);
    }
    
    private void highlightValidMoves(int row, int col) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (gameBoard.isValidMove(row, col, r, c)) {
                    Rectangle rect = (Rectangle) tiles[r][c].getChildren().get(0);
                    Piece targetPiece = gameBoard.getPiece(r, c);
                    if (targetPiece != null) {
                        rect.setStroke(Color.RED);
                    } else {
                        rect.setStroke(Color.LIGHTGREEN);
                    }
                    rect.setStrokeWidth(3);
                }
            }
        }
    }
    
    private void clearSelection() {
        pieceSelected = false;
        selectedRow = -1;
        selectedCol = -1;
        
        // Clear all highlights
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Rectangle rect = (Rectangle) tiles[r][c].getChildren().get(0);
                rect.setStroke(null);
            }
        }
    }
    
    private void updateBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = tiles[row][col];
                
                // Remove old piece label if exists (keep the rectangle)
                while (tile.getChildren().size() > 1) {
                    tile.getChildren().remove(1);
                }
                
                Piece piece = gameBoard.getPiece(row, col);
                if (piece != null) {
                    Label pieceLabel = new Label(piece.getSymbol());
                    pieceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
                    pieceLabel.setTextFill(piece.getColor().equals("white") ? Color.WHITE : Color.BLACK);
                    
                    // Add shadow effect for better visibility
                    if (piece.getColor().equals("white")) {
                        pieceLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 2, 0.5, 1, 1);");
                    } else {
                        pieceLabel.setStyle("-fx-effect: dropshadow(gaussian, white, 2, 0.5, 1, 1);");
                    }
                    
                    tile.getChildren().add(pieceLabel);
                }
            }
        }
    }
    
    private String toChessNotation(int row, int col) {
        return "" + (char) ('a' + col) + (row + 1);
    }
    
    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
