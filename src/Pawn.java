public class Pawn extends Board {
    private int row = 1;
    private int column = 0;
    private static final char SYMBOL = 'P';

    Pawn(String color, int column) {
        addPiece(column, color, row);
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
        if(board[row][pos] != null && board[row][pos].colour.equals(color)){
            throw new IllegalArgumentException("Cannot capture your own piece");
        }
        
        boolean moved = false;
        int oldRow = this.row;
        int oldCol = this.column;
        
        // Double step from start
        if(this.row == 1 && row == 3 && pos == column && board[2][pos] == null && board[3][pos] == null){
            this.row = 3;
            moved = true;
        }
        // Single step forward (no piece blocking)
        else if(row == this.row + 1 && pos == column && board[row][pos] == null){
            this.row++;
            moved = true;
        }
        // Diagonal capture (opponent piece must exist)
        else if(row == this.row + 1 && Math.abs(pos - column) == 1 && board[row][pos] != null){
            this.row = row;
            this.column = pos;
            moved = true;
        }
        
        if(moved){
            board[oldRow][oldCol] = null;
            board[this.row][this.column] = new Index(this.column, color, SYMBOL);
            recordMove(color, new Move(Move.Type.MOVE, this.column, this.row, SYMBOL));
        } else {
            throw new IllegalArgumentException("Invalid pawn move");
        }
    }

    @Override
    void removePiece(int pos, String color, int row) {
        if (board[row][pos] != null) {
            board[row][pos] = null;
            recordMove(color, new Move(Move.Type.REMOVE, pos, row, SYMBOL));
        }
    }
}
