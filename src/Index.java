class Index {
    int currPos;
    boolean exist=false;
    String colour;
    char piece=' ';
    Index(){
        this.colour=null;
    }
    Index(int pos,String color,char piece){
        this.currPos=pos;
        this.colour=color;
        this.piece=piece;
        exist=true;
    }
    
}
