public class Pointt implements Comparable<Pointt>{

	double x ;
	double y ;
	
	public Pointt() {};
	
	public Pointt ( double xn , double yn){
	    x = xn ;
		y = yn;
	
	}
	public int compareTo(Pointt other) {
		if(this.y == other.y){
			if (this.x==other.x)return 0;
			else if (this.x>other.x)return 1 ;
			else return -1;
		}
		else if ( this.y>other.y){
			return 1;
		}
		else {
			return -1;
		}
	}
	
	public String toString(){
		return "(" + x + ","+y+")";
	}
}