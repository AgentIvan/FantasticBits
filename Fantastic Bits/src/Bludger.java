
class Bludger extends Entity{
	
	private int lastEntityId; 


	public Bludger(int id, double x, double y, double vx, double vy, int state, double friction, int radius, int type,
			double mass) {
		super(id, x, y, vx, vy, state, friction, radius, type, mass);
		// TODO Auto-generated constructor stub
	}

	public Bludger(int id, double x, double y, double vx, double vy, int state) {
		super(id, x, y, vx, vy, state);
		// TODO Auto-generated constructor stub
	}

	public Bludger(int id, double x, double y, double vx, double vy) {
		super(id, x, y, vx, vy);
		// TODO Auto-generated constructor stub
	}

	public Bludger(double x, double y) {
		super(x, y);
		// TODO Auto-generated constructor stub
	}
	
	public int getLastEntityId() {
		return lastEntityId;
	}

	public void setLastEntityId(int lastEntityId) {
		this.lastEntityId = lastEntityId;
	}
	

}
