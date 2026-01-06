public class King extends Board {
    private static final char SYMBOL = 'K';
    int row = 0, column;
    
    King(String color, int column) {
        addPiece(column, color, this.row);
    }

    @Override
    void addPiece(int pos, String color, int row) {
        board[row][pos] = new Index(pos, color, SYMBOL);
        this.column = pos;
        this.row = row;
        recordMove(color, new Move(Move.Type.ADD, pos, row, SYMBOL));
    }

    @Override
    void movePiece(int pos, String color, int row) {
        if (pos < 0 || pos >= 8 || row < 0 || row >= 8) {
            throw new IllegalArgumentException("Position out of bounds for king move");
        }
        
        int colDiff = Math.abs(this.column - pos);
        int rowDiff = Math.abs(this.row - row);
        if (colDiff > 1 || rowDiff > 1 || (colDiff == 0 && rowDiff == 0)) {
            throw new IllegalArgumentException("King can only move one square at a time");
        }
        
        if (board[row][pos] != null && board[row][pos].colour.equals(color)) {
            throw new IllegalArgumentException("Cannot capture your own piece");
        }
        
        // Use inherited method from Board
        if (!canKingMoveTo(this.row, this.column, row, pos, color)) {
            throw new IllegalArgumentException("King cannot move to a square under attack");
        }
        
        board[this.row][this.column] = null;
        board[row][pos] = new Index(pos, color, SYMBOL);
        this.row = row;
        this.column = pos;
        recordMove(color, new Move(Move.Type.MOVE, pos, row, SYMBOL));
    }

    @Override
    void removePiece(int pos, String color, int row) {
        if (board[row][pos] != null) {
            board[row][pos] = null;
            recordMove(color, new Move(Move.Type.REMOVE, pos, row, SYMBOL));
        }
    }

    public boolean isInCheck(String color) {
        return isKingInCheck(color);  // inherited from Board
    }

    public int getRow() { return this.row; }
    public int getColumn() { return this.column; }
}
