public class Bishop extends Board{
    private static final char SYMBOL='B';
    int row=0,column;
    Bishop(String color, int column){
        addPiece(column,color,this.row);
    }
    @Override
    void addPiece(int pos, String color, int row) {
        board[row][pos]=new Index(pos,color,SYMBOL);
        this.column=pos;
        this.row=row;
        recordMove(color,new Move(Move.Type.ADD,pos,row,SYMBOL));   
    }
    @Override
    void movePiece(int pos, String color, int row) {
        if(pos < 0 || pos >= 8 || row < 0 || row >= 8){
            throw new IllegalArgumentException("Position out of bounds for bishop move");
        }
        if(board[row][pos] != null && board[row][pos].colour.equals(color)){
            throw new IllegalArgumentException("Cannot capture your own piece");
        }
        int colDiff = Math.abs(this.column - pos);
        int rowDiff = Math.abs(this.row - row);
        if(colDiff != rowDiff){
            throw new IllegalArgumentException("Invalid bishop move");
        }
        
        int colStep = (pos > this.column) ? 1 : -1;
        int rowStep = (row > this.row) ? 1 : -1;
        int c = this.column + colStep;
        int r = this.row + rowStep;
        while(c != pos && r != row){
            if(board[r][c] != null){
                throw new IllegalArgumentException("Path is blocked");
            }
            c += colStep;
            r += rowStep;
        }
        
        board[this.row][this.column] = null;
        board[row][pos] = new Index(pos, color, SYMBOL);
        this.row = row;
        this.column = pos;
        recordMove(color, new Move(Move.Type.MOVE, pos, row, SYMBOL));
    }
    @Override
    void removePiece(int pos, String color, int row) {
        if(board[row][pos] != null){
            board[row][pos] = null;
            recordMove(color, new Move(Move.Type.REMOVE, pos, row, SYMBOL));
        }
    }
}
