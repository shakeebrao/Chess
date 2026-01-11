import java.util.ArrayList;
import java.util.List;

/**
 * Represents the chess game board and handles all game logic.
 * This is a unified board that properly manages piece positions and validates moves.
 */
public class GameBoard {
    
    private Piece[][] board = new Piece[8][8];
    private String currentTurn = "white";
    private List<MoveRecord> moveHistory = new ArrayList<>();
    
    // For en passant tracking
    private int enPassantTargetRow = -1;
    private int enPassantTargetCol = -1;
    
    public GameBoard() {
        initializeBoard();
    }
    
    /**
     * Sets up the initial chess position
     */
    private void initializeBoard() {
        // White pieces (row 0 and 1)
        board[0][0] = new Piece(Piece.Type.ROOK, "white", 0, 0);
        board[0][1] = new Piece(Piece.Type.KNIGHT, "white", 0, 1);
        board[0][2] = new Piece(Piece.Type.BISHOP, "white", 0, 2);
        board[0][3] = new Piece(Piece.Type.QUEEN, "white", 0, 3);
        board[0][4] = new Piece(Piece.Type.KING, "white", 0, 4);
        board[0][5] = new Piece(Piece.Type.BISHOP, "white", 0, 5);
        board[0][6] = new Piece(Piece.Type.KNIGHT, "white", 0, 6);
        board[0][7] = new Piece(Piece.Type.ROOK, "white", 0, 7);
        
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece(Piece.Type.PAWN, "white", 1, col);
        }
        
        // Black pieces (row 6 and 7)
        board[7][0] = new Piece(Piece.Type.ROOK, "black", 7, 0);
        board[7][1] = new Piece(Piece.Type.KNIGHT, "black", 7, 1);
        board[7][2] = new Piece(Piece.Type.BISHOP, "black", 7, 2);
        board[7][3] = new Piece(Piece.Type.QUEEN, "black", 7, 3);
        board[7][4] = new Piece(Piece.Type.KING, "black", 7, 4);
        board[7][5] = new Piece(Piece.Type.BISHOP, "black", 7, 5);
        board[7][6] = new Piece(Piece.Type.KNIGHT, "black", 7, 6);
        board[7][7] = new Piece(Piece.Type.ROOK, "black", 7, 7);
        
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Piece(Piece.Type.PAWN, "black", 6, col);
        }
    }
    
    public Piece getPiece(int row, int col) {
        if (!isValidSquare(row, col)) return null;
        return board[row][col];
    }
    
    public String getCurrentTurn() {
        return currentTurn;
    }
    
    /**
     * Attempts to move a piece from one square to another.
     * Returns true if the move was successful.
     */
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null) return false;
        if (!piece.getColor().equals(currentTurn)) return false;
        
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return false;
        
        // Handle special moves
        Piece captured = board[toRow][toCol];
        boolean isEnPassant = false;
        boolean isCastling = false;
        int rookFromCol = -1, rookToCol = -1;
        
        // En passant capture
        if (piece.getType() == Piece.Type.PAWN && toCol != fromCol && captured == null) {
            if (toRow == enPassantTargetRow && toCol == enPassantTargetCol) {
                isEnPassant = true;
                int capturedPawnRow = piece.getColor().equals("white") ? toRow - 1 : toRow + 1;
                captured = board[capturedPawnRow][toCol];
                board[capturedPawnRow][toCol] = null;
            }
        }
        
        // Castling
        if (piece.getType() == Piece.Type.KING && Math.abs(toCol - fromCol) == 2) {
            isCastling = true;
            if (toCol > fromCol) { // Kingside
                rookFromCol = 7;
                rookToCol = 5;
            } else { // Queenside
                rookFromCol = 0;
                rookToCol = 3;
            }
            // Move rook
            Piece rook = board[fromRow][rookFromCol];
            board[fromRow][rookFromCol] = null;
            board[fromRow][rookToCol] = rook;
            rook.setPosition(fromRow, rookToCol);
        }
        
        // Execute move
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.setPosition(toRow, toCol);
        
        // Update en passant target
        enPassantTargetRow = -1;
        enPassantTargetCol = -1;
        if (piece.getType() == Piece.Type.PAWN && Math.abs(toRow - fromRow) == 2) {
            enPassantTargetRow = (fromRow + toRow) / 2;
            enPassantTargetCol = fromCol;
        }
        
        // Pawn promotion (auto-promote to queen for simplicity)
        if (piece.getType() == Piece.Type.PAWN) {
            if ((piece.getColor().equals("white") && toRow == 7) ||
                (piece.getColor().equals("black") && toRow == 0)) {
                board[toRow][toCol] = new Piece(Piece.Type.QUEEN, piece.getColor(), toRow, toCol);
            }
        }
        
        // Record move
        moveHistory.add(new MoveRecord(piece, fromRow, fromCol, toRow, toCol, captured, isEnPassant, isCastling));
        
        // Switch turn
        currentTurn = currentTurn.equals("white") ? "black" : "white";
        
        return true;
    }
    
    /**
     * Checks if a move is valid (including checking if it would leave king in check)
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null) return false;
        
        // Can't move to a square occupied by own piece
        Piece target = getPiece(toRow, toCol);
        if (target != null && target.getColor().equals(piece.getColor())) return false;
        
        // Check piece-specific move rules
        if (!isPieceMoveLegal(piece, fromRow, fromCol, toRow, toCol)) return false;
        
        // Check if move would leave own king in check
        if (wouldLeaveKingInCheck(fromRow, fromCol, toRow, toCol, piece.getColor())) return false;
        
        return true;
    }
    
    /**
     * Checks if a piece can legally move to the target square (ignoring check)
     */
    private boolean isPieceMoveLegal(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        switch (piece.getType()) {
            case KING:
                return isValidKingMove(piece, fromRow, fromCol, toRow, toCol);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case KNIGHT:
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case PAWN:
                return isValidPawnMove(piece, fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }
    
    private boolean isValidKingMove(Piece king, int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // Normal king move (one square in any direction)
        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0)) {
            return true;
        }
        
        // Castling
        if (rowDiff == 0 && colDiff == 2 && !king.hasMoved()) {
            return canCastle(king, fromRow, fromCol, toCol);
        }
        
        return false;
    }
    
    private boolean canCastle(Piece king, int row, int fromCol, int toCol) {
        String color = king.getColor();
        
        // Can't castle if in check
        if (isInCheck(color)) return false;
        
        int rookCol = toCol > fromCol ? 7 : 0; // Kingside or Queenside
        Piece rook = getPiece(row, rookCol);
        
        // Rook must exist and not have moved
        if (rook == null || rook.getType() != Piece.Type.ROOK || rook.hasMoved()) return false;
        
        // Path must be clear
        int startCol = Math.min(fromCol, rookCol) + 1;
        int endCol = Math.max(fromCol, rookCol);
        for (int col = startCol; col < endCol; col++) {
            if (board[row][col] != null) return false;
        }
        
        // King can't pass through or end up in check
        int direction = toCol > fromCol ? 1 : -1;
        for (int col = fromCol; col != toCol + direction; col += direction) {
            if (isSquareAttacked(row, col, getOpponent(color))) return false;
        }
        
        return true;
    }
    
    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidRookMove(fromRow, fromCol, toRow, toCol) || 
               isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (rowDiff != colDiff || rowDiff == 0) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }
    
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    private boolean isValidPawnMove(Piece pawn, int fromRow, int fromCol, int toRow, int toCol) {
        String color = pawn.getColor();
        int direction = color.equals("white") ? 1 : -1;
        int startRow = color.equals("white") ? 1 : 6;
        
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);
        
        Piece target = getPiece(toRow, toCol);
        
        // Single step forward
        if (colDiff == 0 && rowDiff == direction && target == null) {
            return true;
        }
        
        // Double step from starting position
        if (colDiff == 0 && fromRow == startRow && rowDiff == 2 * direction) {
            int middleRow = fromRow + direction;
            if (board[middleRow][fromCol] == null && target == null) {
                return true;
            }
        }
        
        // Diagonal capture
        if (colDiff == 1 && rowDiff == direction) {
            // Normal capture
            if (target != null && !target.getColor().equals(color)) {
                return true;
            }
            // En passant
            if (toRow == enPassantTargetRow && toCol == enPassantTargetCol) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the path between two squares is clear (for sliding pieces)
     */
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        
        int r = fromRow + rowStep;
        int c = fromCol + colStep;
        
        while (r != toRow || c != toCol) {
            if (board[r][c] != null) return false;
            r += rowStep;
            c += colStep;
        }
        
        return true;
    }
    
    /**
     * Checks if making a move would leave the player's king in check
     */
    private boolean wouldLeaveKingInCheck(int fromRow, int fromCol, int toRow, int toCol, String color) {
        // Save state
        Piece movingPiece = board[fromRow][fromCol];
        Piece capturedPiece = board[toRow][toCol];
        
        // Handle en passant capture for simulation
        Piece enPassantCaptured = null;
        if (movingPiece.getType() == Piece.Type.PAWN && toCol != fromCol && capturedPiece == null) {
            if (toRow == enPassantTargetRow && toCol == enPassantTargetCol) {
                int capturedPawnRow = color.equals("white") ? toRow - 1 : toRow + 1;
                enPassantCaptured = board[capturedPawnRow][toCol];
                board[capturedPawnRow][toCol] = null;
            }
        }
        
        // Simulate move
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = movingPiece;
        
        // Check if king is in check
        boolean inCheck = isInCheck(color);
        
        // Restore state
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        
        // Restore en passant captured pawn
        if (enPassantCaptured != null) {
            int capturedPawnRow = color.equals("white") ? toRow - 1 : toRow + 1;
            board[capturedPawnRow][toCol] = enPassantCaptured;
        }
        
        return inCheck;
    }
    
    /**
     * Checks if the king of the given color is in check
     */
    public boolean isInCheck(String color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;
        return isSquareAttacked(kingPos[0], kingPos[1], getOpponent(color));
    }
    
    /**
     * Checks if the given color is in checkmate
     */
    public boolean isCheckmate(String color) {
        if (!isInCheck(color)) return false;
        return !hasLegalMoves(color);
    }
    
    /**
     * Checks if the given color is in stalemate
     */
    public boolean isStalemate(String color) {
        if (isInCheck(color)) return false;
        return !hasLegalMoves(color);
    }
    
    /**
     * Checks if the player has any legal moves
     */
    private boolean hasLegalMoves(String color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board[fromRow][fromCol];
                if (piece != null && piece.getColor().equals(color)) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Finds the king position for the given color
     */
    private int[] findKing(String color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getType() == Piece.Type.KING && piece.getColor().equals(color)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a square is attacked by any piece of the given color
     */
    private boolean isSquareAttacked(int row, int col, String byColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.getColor().equals(byColor)) {
                    if (canPieceAttack(piece, r, c, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a piece can attack a target square (simplified attack check)
     */
    private boolean canPieceAttack(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        switch (piece.getType()) {
            case KING:
                int rowDiff = Math.abs(toRow - fromRow);
                int colDiff = Math.abs(toCol - fromCol);
                return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case KNIGHT:
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case PAWN:
                // Pawns attack diagonally
                int direction = piece.getColor().equals("white") ? 1 : -1;
                return (toRow - fromRow) == direction && Math.abs(toCol - fromCol) == 1;
            default:
                return false;
        }
    }
    
    private String getOpponent(String color) {
        return color.equals("white") ? "black" : "white";
    }
    
    private boolean isValidSquare(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    /**
     * For debugging: prints the board to console
     */
    public void printBoard() {
        System.out.println("  a b c d e f g h");
        System.out.println("  ---------------");
        for (int r = 7; r >= 0; r--) {
            System.out.print((r + 1) + "|");
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    System.out.print(board[r][c].getNotation() + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println("|" + (r + 1));
        }
        System.out.println("  ---------------");
        System.out.println("  a b c d e f g h");
    }
    
    /**
     * Record of a move for history/undo functionality
     */
    private static class MoveRecord {
        Piece piece;
        int fromRow, fromCol, toRow, toCol;
        Piece captured;
        boolean isEnPassant;
        boolean isCastling;
        
        MoveRecord(Piece piece, int fromRow, int fromCol, int toRow, int toCol, 
                   Piece captured, boolean isEnPassant, boolean isCastling) {
            this.piece = piece;
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.captured = captured;
            this.isEnPassant = isEnPassant;
            this.isCastling = isCastling;
        }
    }
}
