import java.util.*;
import java.io.*;
import java.math.*;


/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        int magic = 0;
        Point[] goals = new Point[2];

        goals[0] = new Point(16000, 3750); //Right goal center
        goals[1] = new Point(0, 3750); //Left goal center
        
        Point[] topGoalPoints = new Point[2];
        topGoalPoints[0] = new Point(16000,1900);
        topGoalPoints[1] = new Point(0,1900);
        
        Point[] bottomGoalPoints = new Point[2];
        bottomGoalPoints[0] = new Point(16000,5600);
        bottomGoalPoints[1] = new Point(0,5600);
        
        Movement[] movements = new Movement[4];
        
        // game loop
        while (true) {
            
            List<Entity> wizards = new ArrayList<Entity>();
            List<Entity> opponentWizards = new ArrayList<Entity>();
            List<Entity> snaffles = new ArrayList<Entity>();
            List<Entity> allwizards = new ArrayList<Entity>();
            
            int entities = in.nextInt(); // number of entities still in game
            
            
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise
                
                Entity newEntity;
                
                switch (entityType) {
				case "WIZARD":
					newEntity = new Entity(entityId,x,y,vx,vy,state, 0.75, 400, 0, 1);
					wizards.add(newEntity);
					break;
				case "OPPONENT_WIZARD":
	                newEntity = new Entity(entityId,x,y,vx,vy,state, 0.75, 400, 0, 1);
					opponentWizards.add(newEntity);
					break;
				case "SNAFFLE":
	                newEntity = new Entity(entityId,x,y,vx,vy,state, 0.75, 150, 1, 0.5);
					snaffles.add(newEntity);
					break;
				case "BLUDGER":
					break;
				default:
					break;
				}
                
            }
            
            
            //----------------------------------------------------
            //------------Simulate collisions---------------------
            allwizards.addAll(wizards);
            allwizards.addAll(opponentWizards);
            System.err.println("------------------BEFORE-----------------");
            printList(allwizards);
            play(allwizards, movements);
            System.err.println("------------------AFTER-----------------");
            printList(allwizards);
            
            //print movements
            for(int i=0; i<4; i++) {
            	if(movements[i]!=null) {
            		System.err.println("Movement " + i);
            		movements[i].print();
            	}
            }
            
            //Reset movements
            for(int i=0; i<4; i++) {
            	movements[i] = null;
            }
            
            //---------------------------------------------------
            

            
            int chasestSnaffleId = -1;
            
            
            Entity[] snafflesToCatch = findNearestSnaffles(wizards, snaffles);
            
            for(Entity wizard : wizards) {
            	
            	
                if(wizard.getState() == 0) {
                    Entity nearestSnaffle = snafflesToCatch[wizard.getId()%2];
                    
                    Point wizardNextPosition = wizard.computeNextPositionInNbTour(1);
                    Point snaffleNextPosition = nearestSnaffle.computeNextPositionInNbTour(1);
                    
                    List<Entity> snafflesGoingToGoal = searchSnafflesGoingToGoal(snaffles, topGoalPoints[1-myTeamId], bottomGoalPoints[1-myTeamId]);
                    boolean spellLaunched = false;
                    
                    if(snafflesGoingToGoal.size()>0 && magic>=10) {
                    	Entity snaffleToStop = snafflesGoingToGoal.get(0);
                    	petrifcus(snaffleToStop);
                    	snaffleToStop.stop();
                    	spellLaunched = true;
                    }
                    

                    
                    if(magic >= 20 && !spellLaunched) {
                        /*if(wizard.isUsefulToAccio(nearestSnaffle, goals[myTeamId])){
                        	actio(nearestSnaffle);
                        	magic-=20;
                        	spellLaunched = true;
                        } else*/ if(lineLineIntersect(wizardNextPosition, snaffleNextPosition, topGoalPoints[myTeamId], bottomGoalPoints[myTeamId] )){
                        	
                        	boolean ennemyInTrajectory = false;
                        	
                        	for(Entity opponentWizard : opponentWizards) {
                        		Point opponentNextPosition = opponentWizard.computeNextPositionInNbTour(1);
                        		if(lineCircleIntersect(wizardNextPosition, snaffleNextPosition, opponentNextPosition , 400)) {
                        			ennemyInTrajectory = true;
                        		}
                        	}
                        	if(!ennemyInTrajectory) {
    	                    	flipendo(nearestSnaffle);
    	                    	
    	                    	System.err.println("Wizard next pos:");
    	                    	wizardNextPosition.print();
    	                    	System.err.println("Snaffle next pos:");
    	                    	snaffleNextPosition.print();
    	                    	
    	                    	
    	                    	magic-=20;
    	                    	spellLaunched = true;
                        	}
                        }
                    }
                    
                    if(!spellLaunched) {
                        moveTo(nearestSnaffle, 150);
                        movements[wizard.getId()]= new Movement(wizard,(Point) nearestSnaffle, 150);
                    }
                    
                    if(snaffles.size()>1) {
                    	chasestSnaffleId = nearestSnaffle.getId();
                    }
                } else {
                    throwTo(goals[myTeamId], 500);
                }
                

                
            }
            magic++;
            
        }
    }
    
    public static void printList(List<Entity> entities) {
    	for(Entity entity : entities) {
    		entity.print();
    	}
    }
    
    public static void moveTo(Entity entity, int power) {
        System.out.print("MOVE ");
        System.out.print(entity.getX());
        System.out.print(" ");
        System.out.print(entity.getY());
        System.out.print(" ");
        System.out.println(power);
    }
    
    public static void throwTo(Point point, int power) {
        System.out.print("THROW ");
        System.out.print(point.getX());
        System.out.print(" ");
        System.out.print(point.getY());
        System.out.print(" ");
        System.out.println(power);
    }
    
    public static void actio(Entity entity) {
    	System.out.print("ACCIO ");
        System.out.println(entity.getId());
    }
    
    public static void flipendo(Entity entity) {
    	System.out.print("FLIPENDO ");
        System.out.println(entity.getId());
    }
    
    public static void petrifcus(Entity entity) {
    	System.out.print("PETRIFICUS ");
        System.out.println(entity.getId());
    }
    
    public static Entity[] findNearestSnaffles(List<Entity> wizards, List<Entity> snaffles) {
    	
    	if(snaffles.size()>1) {
	    	Entity[] snaffles1 = new Entity[2];
	    	
	    	snaffles1[0] = wizards.get(0).searchNearestSnaffle(snaffles);
	    	//snaffles1[1] = wizards.get(1).searchNearestSnaffleExcept(snaffles, snaffles1[0].getId());
	    	snaffles1[1] = wizards.get(1).searchNearestSnaffle(snaffles);
	    	
	    	int totalDistanceSquare1 = wizards.get(0).computeDistanceSquare(snaffles1[0]);
	    	totalDistanceSquare1+= wizards.get(1).computeDistanceSquare(snaffles1[1]);
	    	
	    	Entity[] snaffles2 = new Entity[2];
	    	
	    	snaffles2[1] = wizards.get(1).searchNearestSnaffle(snaffles);
	    	//snaffles2[0] = wizards.get(0).searchNearestSnaffleExcept(snaffles, snaffles2[1].getId());
	    	snaffles2[0] = wizards.get(0).searchNearestSnaffle(snaffles);
	    	
	    	int totalDistanceSquare2 = wizards.get(0).computeDistanceSquare(snaffles2[0]);
	    	totalDistanceSquare2+= wizards.get(1).computeDistanceSquare(snaffles2[1]);
	    	
	    	if(totalDistanceSquare1 > totalDistanceSquare2) {
	    		return snaffles2;
	    	} else {
	        	return snaffles1;
	    	}
    	}else {
    		return new Entity[]{snaffles.get(0), snaffles.get(0)};
    	}
    	

    }
    
    /**
     * Check if segment [Point1, Point2] intersect PERPANDICULAR segment [Point3, Point4] (the goal)
     * 
     * WARNING Point3.y has to be inferior to Point4.y
     * 
     * @param point1 the wizard
     * @param point2 the snaffle
     * @param point3 the top goal
     * @param point4 the bottom goal
     * @return
     */
    public static boolean lineLineIntersect(Point point1, Point point2, Point point3, Point point4) {
    	if(point2.isBetweenInX(point1, point3)) {
	    	double a= ((double)(point2.getY()-point1.getY()))/((double)(point2.getX()- point1.getX()));
	    	double b= point1.getY() - a*point1.getX();
	    	
	    	System.err.println("a: " + a);
	    	System.err.println("b: " + b);
	    	
	    	double yTarget = a*point3.getX()+b;
	    	System.err.println("yTarget: " + yTarget);
	    	
	    	if(yTarget>point3.getY() && yTarget<point4.getY()) {
	    		return true;
	    	}
        }
    	return false;
    }

    
    public static boolean lineCircleIntersect(Point A, Point B, Point C, int radius) {
    	
    	float Ax = A.getX();
    	float Ay = A.getY();
    	float Bx = B.getX();
    	float By = B.getY();
    	float Cx = C.getX();
    	float Cy = C.getY();

    	
    	// compute the euclidean distance between A and B
    	float LAB = (float) Math.sqrt( (Bx-Ax)*(Bx-Ax)+(By-Ay)*(By-Ay) );

    	// compute the direction vector D from A to B
    	float Dx = (Bx-Ax)/LAB;
    	float Dy = (By-Ay)/LAB;

    	// Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.

    	// compute the value t of the closest point to the circle center (Cx, Cy)
    	float t = Dx*(Cx-Ax) + Dy*(Cy-Ay); 

    	// This is the projection of C on the line from A to B.

    	// compute the coordinates of the point E on line and closest to C
    	float Ex = t*Dx+Ax;
    	float Ey = t*Dy+Ay;

    	// compute the euclidean distance from E to C
    	float LEC = (float) Math.sqrt( (Ex-Cx)*(Ex-Cx)+(Ey-Cy)*(Ey-Cy) );

    	// test if the line intersects the circle
    	if( LEC < radius )
    	{
    	    /*// compute distance from t to circle intersection point
    	    dt = sqrt( R² - LEC²)

    	    // compute first intersection point
    	    Fx = (t-dt)*Dx + Ax
    	    Fy = (t-dt)*Dy + Ay

    	    // compute second intersection point
    	    Gx = (t+dt)*Dx + Ax
    	    Gy = (t+dt)*Dy + Ay
    	}*/
    		return true;
    	}
    	return false;
    }
    
    public static List<Entity> searchSnafflesGoingToGoal(List<Entity> snaffles, Point topGoalPoint, Point bottomGoalPoint){
    	
    	List<Entity> snafflesGoingToGoal = new ArrayList<Entity>();
    	
    	for(Entity snaffle : snaffles) {
    		
    		if(snaffle.getVx()!=0) {
	    		Point nextPosition = snaffle.computeNextPositionInNbTour(1);
	    		if(nextPosition.isInsideGame()) {
	    			Point positionInNTour = snaffle.computeNextPositionInNbTour(3);
	    			
	    			//If snaffle is in the goal in N tour
		    		if(positionInNTour.getX()==topGoalPoint.getX()) {
		    			snafflesGoingToGoal.add(snaffle);
		    		}
	    		}
    		}
    	}
    	
    	Collections.sort(snafflesGoingToGoal, new Comparator<Entity>() {

			@Override
			public int compare(Entity o1, Entity o2) {
				int velocity1 = Math.abs(o1.getVx());
				int velocity2 =Math.abs( o2.getVx());
				
				
				return Integer.compare(velocity2, velocity1);
			}
        });
    	
    	return snafflesGoingToGoal;
    }
    
    
    static void play(List<Entity> wizards, Movement[] movements) {
    	
    	
    	for(int i=0; i<4; i++) {
    		if(movements[i]!=null) {
    			wizards.get(i).addMovement(movements[i]);
    		}
    	}
    	
        // This tracks the time during the turn. The goal is to reach 1.0
        double t = 0.0;

        while (t < 1.0) {
            Collision firstCollision = null;

            // We look for all the collisions that are going to occur during the turn
            for (int i = 0; i < wizards.size(); ++i) {
                // Collision with another pod?
                for (int j = i + 1; j < wizards.size(); ++j) {
                    Collision col = wizards.get(i).collision(wizards.get(j));

                    // If the collision occurs earlier than the one we currently have we keep it
                    if (col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
                        firstCollision = col;
                        
                        System.err.println("COLLISION between " + wizards.get(i).getId() + " and " + wizards.get(j).getId());
                    }
                }
            }

            if (firstCollision == null) {
                // No collision, we can move the pods until the end of the turn
                for (int i = 0; i < wizards.size(); ++i) {
                	//TODO move the wizard after collision
                    wizards.get(i).move(1.0 - t);
                }

                // End of the turn
                t = 1.0;
            } else {
                // Move the pods to reach the time `t` of the collision
                for (int i = 0; i < wizards.size(); ++i) {
                	//TODO move the wizard after collision
                    wizards.get(i).move(firstCollision.t - t);
                }

                // Play out the collision
                firstCollision.entityA.bounce(firstCollision.entityB);

                t += firstCollision.t;
            }
        }

        /*for (int i = 0; i < wizards.length; ++i) {
            wizards[i].end();
        }*/
    }

}
