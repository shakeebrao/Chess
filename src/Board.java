import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Board extends Index {
    Index[][] board = new Index[8][8];
    private final Map<String, List<Move>> moveHistory = new HashMap<>();
    private Move mostRecentMove;
    private String mostRecentColor;

    protected Board() {
        moveHistory.put("white", new ArrayList<>());
        moveHistory.put("black", new ArrayList<>());
    }

    protected void recordMove(String color, Move move) {
        String normalized = normalizeColor(color);
        moveHistory.computeIfAbsent(normalized, k -> new ArrayList<>()).add(move);
        mostRecentMove = move;
        mostRecentColor = normalized;
    }

    protected List<Move> getMovesFor(String color) {
        List<Move> moves = moveHistory.get(normalizeColor(color));
        return moves == null ? Collections.emptyList() : Collections.unmodifiableList(moves);
    }

    protected Move getLatestMoveFor(String color) {
        List<Move> moves = moveHistory.get(normalizeColor(color));
        if (moves == null || moves.isEmpty()) {
            return null;
        }
        return moves.get(moves.size() - 1);
    }

    protected Move getMostRecentMove() {
        return mostRecentMove;
    }

    protected String getMostRecentMoveColor() {
        return mostRecentColor;
    }

    protected Map<String, List<Move>> getMoveHistorySnapshot() {
        Map<String, List<Move>> snapshot = new HashMap<>();
        moveHistory.forEach((color, moves) -> snapshot.put(color, new ArrayList<>(moves)));
        return snapshot;
    }

    private String normalizeColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color cannot be null");
        }
        return color.trim().toLowerCase();
    }

    void addPiece(int pos, String color, int row) {};
    void movePiece(int pos, String color, int row) {};
    void removePiece(int pos, String color, int row) {};

    // Print board as chess board with piece characters
    protected void printBoard() {
        System.out.println("  0 1 2 3 4 5 6 7");
        System.out.println("  ---------------");
        for (int r = 7; r >= 0; r--) {
            System.out.print(r + "|");
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    System.out.print(board[r][c].piece + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println("|" + r);
        }
        System.out.println("  ---------------");
        System.out.println("  0 1 2 3 4 5 6 7");
    }

    // -------- Check validation methods --------

    // Find king position for a color, returns [row, col] or null if not found
    protected int[] findKing(String color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Index piece = board[r][c];
                if (piece != null && piece.piece == 'K' 
                    && piece.colour != null && piece.colour.equalsIgnoreCase(color)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    // Check if a square is attacked by any piece of given color
    protected boolean isSquareAttacked(int row, int col, String byColor) {
        return isAttackedByPawn(row, col, byColor)
            || isAttackedByKnight(row, col, byColor)
            || isAttackedByBishop(row, col, byColor)
            || isAttackedByRook(row, col, byColor)
            || isAttackedByQueen(row, col, byColor)
            || isAttackedByKing(row, col, byColor);
    }

    // Check if king of given color is in check
    protected boolean isKingInCheck(String kingColor) {
        int[] kingPos = findKing(kingColor);
        if (kingPos == null) return false;
        String opponent = kingColor.equalsIgnoreCase("white") ? "black" : "white";
        return isSquareAttacked(kingPos[0], kingPos[1], opponent);
    }

    // Check if moving a piece would expose king (pinned piece check)
    protected boolean wouldExposeKing(int fromRow, int fromCol, int toRow, int toCol, String color) {
        // Save current state
        Index movingPiece = board[fromRow][fromCol];
        Index targetPiece = board[toRow][toCol];
        
        // Simulate move
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = movingPiece;
        
        // Check if king is in check after move
        boolean exposed = isKingInCheck(color);
        
        // Restore state
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = targetPiece;
        
        return exposed;
    }

    // Check if king can safely move to target square
    protected boolean canKingMoveTo(int kingRow, int kingCol, int targetRow, int targetCol, String kingColor) {
        if (targetRow < 0 || targetRow >= 8 || targetCol < 0 || targetCol >= 8) {
            return false;
        }
        
        Index targetSquare = board[targetRow][targetCol];
        if (targetSquare != null && targetSquare.colour != null 
            && targetSquare.colour.equalsIgnoreCase(kingColor)) {
            return false;
        }
        
        // Simulate king move
        Index king = board[kingRow][kingCol];
        board[kingRow][kingCol] = null;
        Index captured = board[targetRow][targetCol];
        board[targetRow][targetCol] = king;
        
        String opponent = kingColor.equalsIgnoreCase("white") ? "black" : "white";
        boolean safe = !isSquareAttacked(targetRow, targetCol, opponent);
        
        // Restore
        board[kingRow][kingCol] = king;
        board[targetRow][targetCol] = captured;
        
        return safe;
    }

    // -------- Attack detection helpers --------

    private boolean isAttackedByPawn(int row, int col, String byColor) {
        int pawnRow = byColor.equalsIgnoreCase("white") ? row - 1 : row + 1;
        if (pawnRow < 0 || pawnRow >= 8) return false;
        
        for (int dc : new int[]{-1, 1}) {
            int pawnCol = col + dc;
            if (pawnCol >= 0 && pawnCol < 8) {
                Index p = board[pawnRow][pawnCol];
                if (p != null && p.piece == 'P' && p.colour.equalsIgnoreCase(byColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAttackedByKnight(int row, int col, String byColor) {
        int[][] moves = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] m : moves) {
            int r = row + m[0], c = col + m[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Index p = board[r][c];
                if (p != null && p.piece == 'N' && p.colour.equalsIgnoreCase(byColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAttackedByBishop(int row, int col, String byColor) {
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        return checkSlidingAttack(row, col, byColor, dirs, 'B');
    }

    private boolean isAttackedByRook(int row, int col, String byColor) {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        return checkSlidingAttack(row, col, byColor, dirs, 'R');
    }

    private boolean isAttackedByQueen(int row, int col, String byColor) {
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1},{-1,0},{1,0},{0,-1},{0,1}};
        return checkSlidingAttack(row, col, byColor, dirs, 'Q');
    }

    private boolean checkSlidingAttack(int row, int col, String byColor, int[][] dirs, char piece) {
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Index p = board[r][c];
                if (p != null) {
                    if (p.piece == piece && p.colour.equalsIgnoreCase(byColor)) return true;
                    break;
                }
                r += d[0];
                c += d[1];
            }
        }
        return false;
    }

    private boolean isAttackedByKing(int row, int col, String byColor) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr, c = col + dc;
                if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                    Index p = board[r][c];
                    if (p != null && p.piece == 'K' && p.colour.equalsIgnoreCase(byColor)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
