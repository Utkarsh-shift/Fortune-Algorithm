public class Edge
{
    Pointt start ;
	Pointt end;
	Pointt site_left;
	Pointt site_right;
	Pointt direction ;
	Pointt mid ;
	Edge neighbor ;
	double slope ;
	double yint ;
	public Edge ( Pointt start2 , Pointt point , Pointt p){
		start = start2 ;
		site_left = point ;
		site_right = p ;
		direction = new Pointt ( p.y - point.y , -( p.x-point.x));
	    end = null;
	    slope = (p.x - point.y)/(point.y-p.y);
	    Pointt mid = new Pointt (( p.x+point.x)/2,(p.y + point.y)/2);
	    yint = mid.y - slope*mid.x;
	}
public String toString(){
	return start + ","+end;
}
}