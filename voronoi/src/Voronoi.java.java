import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Voronoi {
	
	List <Pointt> sites;
	List <Edge> edges; 
	PriorityQueue<Event> events; // sweep line
	Parabola root; //  beach line
	
	// size of window
	double width = 1;
	double height = 1;
	
	double ycurr; // current y-coord of sweep line
	
	public Voronoi (List <Pointt> sites) {
		this.sites = sites;
		edges = new ArrayList<Edge>();
		generateVoronoi();
	}
	
	private void generateVoronoi() {
		
		events = new PriorityQueue <Event>();
		for (Pointt p : sites) {
			events.add(new Event(p, Event.site_Event));
		}
		
		// process events (sweep line)
		int count = 0;
		while (!events.isEmpty()) {
			Event e = events.remove();
			ycurr = e.p.y;
			count++;
			if (e.type == Event.site_Event) {
				System.out.println(count + ". SITE_EVENT " + e.p);
				handleSite(e.p);
			}
			else {
				System.out.println(count + ". CIRCLE_EVENT " + e.p);
				handleCircle(e);
			}
		}
		
		ycurr = width+height;
		
		endEdges(root); 
		
		
		for (Edge e: edges){
			if (e.neighbor != null) {
				e.start = e.neighbor.end;
				e.neighbor = null;
			}
		}
	}

	
	private void endEdges(Parabola p) {
		if (p.type == Parabola.IS_FOCUS) {
			p = null;
			return;
		}

		double x = getXofEdge(p);
		p.edge.end = new Pointt (x, p.edge.slope*x+p.edge.yint);
		edges.add(p.edge);
		
		endEdges(p.child_left);
		endEdges(p.child_right);
		
		p = null;
	}

	// processes site event
	private void handleSite(Pointt p) {

		if (root == null) {
			root = new Parabola(p);
			return;
		}
		
		// find parabola on beach line right above p
		Parabola par = getParabolaByX(p.x);
		if (par.event != null) {
			events.remove(par.event);
			par.event = null;
		}
		Pointt start = new Pointt(p.x, getY(par.point, p.x));
		Edge el = new Edge(start, par.point, p);
		Edge er = new Edge(start, p, par.point);
		el.neighbor = er;
		er.neighbor = el;
		par.edge = el;
		par.type = Parabola.IS_VERTEX;
		
		Parabola p0 = new Parabola (par.point);
		Parabola p1 = new Parabola (p);
		Parabola p2 = new Parabola (par.point);

		par.setLeftChild(p0);
		par.setRightChild( new Parabola());
		par.child_right.edge = er;
		par.child_right.setLeftChild(p1);
		par.child_right.setRightChild(p2);

		checkCircleEvent(p0);
		checkCircleEvent(p2);
	}
	
	// process circle event
	private void handleCircle(Event e) {
		
		// find p0, p1, p2 that generate this event from left to right
		Parabola p1 = e.arc;
		Parabola xl = Parabola.getLeftParent(p1);
		Parabola xr = Parabola.getRightParent(p1);
		Parabola p0 = Parabola.getLeftChild(xl);
		Parabola p2 = Parabola.getRightChild(xr);
		
		// remove associated events since the points will be altered
		if (p0.event != null) {
			events.remove(p0.event);
			p0.event = null;
		}
		if (p2.event != null) {
			events.remove(p2.event);
			p2.event = null;
		}
		
		Pointt p = new Pointt(e.p.x, getY(p1.point, e.p.x)); // new vertex
	
		// end edges!
		xl.edge.end = p;
		xr.edge.end = p;
		edges.add(xl.edge);
		edges.add(xr.edge);

		// start new bisector (edge) from this vertex on which ever original edge is higher in tree
		Parabola higher = new Parabola();
		Parabola par = p1;
		while (par != root) {
			par = par.parent;
			if (par == xl) higher = xl;
			if (par == xr) higher = xr;
		}
		higher.edge = new Edge(p, p0.point, p2.point);
		
		// delete p1 and parent (boundary edge) from beach line
		Parabola gparent = p1.parent.parent;
		if (p1.parent.child_left == p1) {
			if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_right);
			if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_right);
		}
		else {
			if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_left);
			if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_left);
		}

		Pointt op = p1.point;
		p1.parent = null;
		p1 = null;
		
		checkCircleEvent(p0);
		checkCircleEvent(p2);
	}
	
	// adds circle event if foci a, b, c lie on the same circle
	private void checkCircleEvent(Parabola b) {

		Parabola lp = Parabola.getLeftParent(b);
		Parabola rp = Parabola.getRightParent(b);

		if (lp == null || rp == null) return;
		
		Parabola a = Parabola.getLeftChild(lp);
		Parabola c = Parabola.getRightChild(rp);
	
		if (a == null || c == null || a.point == c.point) return;

		if (ccw(a.point,b.point,c.point) != 1) return;
		
		// edges will intersect to form a vertex for a circle event
		Pointt start = getEdgeIntersection(lp.edge, rp.edge);
		if (start == null) return;
		
		// compute radius
		double dx = b.point.x - start.x;
		double dy = b.point.y - start.y;
		double d = Math.sqrt((dx*dx) + (dy*dy));
		if (start.y + d < ycurr) return; // must be after sweep line

		Pointt ep = new Pointt(start.x, start.y + d);
	//	System.out.println("added circle event "+ ep);

		// add circle event
		Event e = new Event (ep, Event.circle_Event);
		e.arc = b;
		b.event = e;
		events.add(e);
	}

	public int ccw(Pointt a, Pointt b, Pointt c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return  0;
    }

	// returns intersection of the lines of with vectors a and b
	private Pointt getEdgeIntersection(Edge a, Edge b) {

		if (b.slope == a.slope && b.yint != a.yint) return null;

		double x = (b.yint - a.yint)/(a.slope - b.slope);
		double y = a.slope*x + a.yint;

		return new Pointt(x, y);
	} 
	
	// returns current x-coordinate of an unfinished edge
	private double getXofEdge (Parabola par) {
		//find intersection of two parabolas
		
		Parabola left = Parabola.getLeftChild(par);
		Parabola right = Parabola.getRightChild(par);
		
		Pointt p = left.point;
		Pointt r = right.point;
		
		double dp = 2*(p.y - ycurr);
		double a1 = 1/dp;
		double b1 = -2*p.x/dp;
		double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;
		
		double dp2 = 2*(r.y - ycurr);
		double a2 = 1/dp2;
		double b2 = -2*r.x/dp2;
		double c2 = (r.x*r.x + r.y*r.y - ycurr*ycurr)/dp2;
		
		double a = a1-a2;
		double b = b1-b2;
		double c = c1-c2;
		
		double disc = b*b - 4*a*c;
		double x1 = (-b + Math.sqrt(disc))/(2*a);
		double x2 = (-b - Math.sqrt(disc))/(2*a);
		
		double ry;
		if (p.y > r.y) ry = Math.max(x1, x2);
		else ry = Math.min(x1, x2);
		
		return ry;
	}
	
	// returns parabola above this x coordinate in the beach line
	private Parabola getParabolaByX (double xx) {
		Parabola par = root;
		double x = 0;
		while (par.type == Parabola.IS_VERTEX) {
			x = getXofEdge(par);
			if (x>xx) par = par.child_left;
			else par = par.child_right;
		}
		return par;
	}
	
	// find corresponding y-coordinate to x on parabola with focus p
	private double getY(Pointt p, double x) {
		// determine equation for parabola around focus p
		double dp = 2*(p.y - ycurr);
		double a1 = 1/dp;
		double b1 = -2*p.x/dp;
		double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;
		return (a1*x*x + b1*x + c1);
	}

	public static void main(String[] args) {
		
		//int N = Integer.parseInt(args[0]);
        Scanner n = new Scanner(System.in) ;
        int N = n.nextInt();
		Stopwatch s = new Stopwatch();

		ArrayList<Pointt> points = new ArrayList<Pointt>();

		Random gen = new Random();
			
		for (int i = 0; i < N; i++){
			double x = gen.nextDouble();
			double y = gen.nextDouble();
			points.add(new Pointt(x, y));
		}

		double start = s.elapsedTime();
		Voronoi diagram = new Voronoi (points);
		double stop = s.elapsedTime();
        System.out.println("Time taken to compute diagram of " + N + " sites = ");
		System.out.println(stop-start);

	
		StdDraw.setPenRadius(.005);
		for (Pointt p: points) {
			StdDraw.point(p.x, p.y);
		}
		StdDraw.setPenRadius(.002);
		for (Edge e: diagram.edges) {
			StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
		}
		
	}
}