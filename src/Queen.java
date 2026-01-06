public class Queen extends Board{
    private static final char SYMBOL='Q';
    int row=0,column;
    Queen(String color,int column){
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
            throw new IllegalArgumentException("Position out of bounds for queen move");
        }
        if(board[row][pos] != null && board[row][pos].colour.equals(color)){
            throw new IllegalArgumentException("Cannot capture your own piece");
        }
        
        boolean moved = false;
        int oldRow = this.row;
        int oldCol = this.column;
        
        int colDiff = Math.abs(this.column - pos);
        int rowDiff = Math.abs(this.row - row);
        
        // Diagonal move
        if(colDiff == rowDiff && colDiff != 0){
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
            this.column = pos;
            this.row = row;
            moved = true;
        }
        // Horizontal move
        else if(this.row == row && this.column != pos){
            int step = (pos > this.column) ? 1 : -1;
            for(int c = this.column + step; c != pos; c += step){
                if(board[row][c] != null){
                    throw new IllegalArgumentException("Path is blocked");
                }
            }
            this.column = pos;
            moved = true;
        }
        // Vertical move
        else if(this.column == pos && this.row != row){
            int step = (row > this.row) ? 1 : -1;
            for(int r = this.row + step; r != row; r += step){
                if(board[r][pos] != null){
                    throw new IllegalArgumentException("Path is blocked");  
                }
            }
            this.row = row;
            moved = true;
        }
        if(moved){
            board[oldRow][oldCol] = null;
            board[this.row][this.column] = new Index(this.column, color, SYMBOL);
            recordMove(color, new Move(Move.Type.MOVE, this.column, this.row, SYMBOL));
        } else {
            throw new IllegalArgumentException("Invalid queen move");
        }
    }
    @Override
    void removePiece(int pos, String color, int row) {
        if(board[row][pos] != null){
            board[row][pos] = null;
            recordMove(color, new Move(Move.Type.REMOVE, pos, row, SYMBOL));
        }
    }
}
