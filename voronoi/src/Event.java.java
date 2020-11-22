public  class Event implements Comparable <Event>{

	public static int site_Event = 0;
	public static int circle_Event = 1 ;
	
	
	Pointt p ;
	int type ;
	Parabola arc;
	public Event ( Pointt p , int type ){
		this.p=p;
		this.type = type ;
		arc = null ;
	
	}
public int compareTo(Event other ){
	return this.p.compareTo(other.p) ; // Re_check 
	}
}