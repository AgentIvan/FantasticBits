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
        
        HashMap<Integer, Entity> bludgers = new HashMap<Integer, Entity>();
        HashMap<Integer, Entity> snafflesMap = new HashMap<Integer, Entity>();
        HashMap<Integer, Entity> wizardsMap = new HashMap<Integer, Entity>();
        
        // game loop
        while (true) {
            
            //List<Entity> wizards = new ArrayList<Entity>();
            List<Entity> opponentWizards = new ArrayList<Entity>();
            //List<Entity> snaffles = new ArrayList<Entity>();
            List<Entity> wizardsAndBludgers = new ArrayList<Entity>();
            
            
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
					
					Entity entity = wizardsMap.get(entityId);
					if(entity==null) {
						entity = new Entity(entityId,x,y,vx,vy,state, 0.75, 400, 0, 1);
						wizardsMap.put(entityId, entity);
					} else {
						entity.update(x,y,vx,vy,state);
					}
					
					break;
				case "OPPONENT_WIZARD":
	                newEntity = new Entity(entityId,x,y,vx,vy,state, 0.75, 400, 0, 1);
					opponentWizards.add(newEntity);
					break;
				case "SNAFFLE":
	                //newEntity = new Snaffle(entityId,x,y,vx,vy,state, 0.75, 150, 1, 0.5);
					//snaffles.add(newEntity);
					
					
					Snaffle snaffle = (Snaffle) snafflesMap.get(entityId);
					if(snaffle==null) {
						snaffle = new Snaffle(entityId,x,y,vx,vy,state, 0.75, 150, 1, 0.5);
						snafflesMap.put(entityId, snaffle);
						//newEntity.printAll();
					} else {
						snaffle.update(x,y,vx,vy);
						//bludger.printAll();
					}
					
					break;
				case "BLUDGER":
					
					Bludger bludger = (Bludger) bludgers.get(entityId);
					if(bludger==null) {
						bludger = new Bludger(entityId,x,y,vx,vy,state, 0.9, 200, 2, 8);
						bludger.setLastEntityId(-1);
						bludgers.put(entityId, bludger);
						//newEntity.printAll();
					} else {
						bludger.update(x,y,vx,vy);
						//bludger.printAll();
					}
	                
					
	                
					break;
				default:
					break;
				}
                
            }
            
            List<Entity> snaffles = new ArrayList<Entity>(snafflesMap.values());
            List<Entity> wizards = new ArrayList<Entity>(wizardsMap.values());
            
            
            //Reset movements
            for(int i=0; i<4; i++) {
            	movements[i] = null;
            }
            

           
            //-------------------------------------------------Algorithm to move or launch spells with 2 wizards-------------------------------

            
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
    	                    	
    	                    	// Add flipendo effect to the snaffle
    	                    	Snaffle snaffle = (Snaffle) nearestSnaffle;
    	                    	snaffle.flipendo(wizard);
    	                    	
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
                	//wizard.setState(0);
                    throwTo(goals[myTeamId], 500);
                    Snaffle snaffle = (Snaffle) wizard.getSnaffleCarried();
                    System.err.println("SNAFFLE : " + snaffle.getId());
                    snaffle.throwToPosition(goals[myTeamId], 500);
                }
                

                
            }
            magic++;
            
            
            //----------------------------------------Predict movement of next turn----------------------------------------------

             
             
             //----------------------------------------------------
             //------------Simulate collisions---------------------
             wizardsAndBludgers.addAll(wizards);
             wizardsAndBludgers.addAll(opponentWizards);
             
             


             //Move all wizards
             //FIXME we move only our wizards here
         	for(int i=0; i<2; i++) {
         		if(movements[i]!=null) {
         			wizards.get(i).addMovement(movements[i]);
         		}
         	}
         	
         	//Move all bludger
         	for(Entity entity : bludgers.values()) {
         		Bludger bludger = (Bludger) entity;
         		Entity wizardChased = bludger.searchNearestEntityExcept(wizardsAndBludgers, bludger.getLastEntityId());
         		Movement movement = new Movement(entity, wizardChased, 1000);
         		bludger.addMovement(movement);
         	}
         	
         	//Apply spells to all the snaffles
         	for(Entity entity : snaffles) {
         		Snaffle snaffle = (Snaffle) entity;
         		snaffle.applySpells();
         	}
         	
         	
         	wizardsAndBludgers.addAll(bludgers.values());
         	wizardsAndBludgers.addAll(snaffles);
         	
         	//printList(allwizards);
             play(wizardsAndBludgers);
             
             //Update position of snaffle catched by wizards
             for(Entity entity : snaffles) {
             	Snaffle snaffle = (Snaffle) entity;
             	snaffle.updatePosition();
             }
             
             System.err.println("------------------AFTER-----------------");
             printList(wizardsAndBludgers);
             
             //---------------------------------------------------
            
        }
    }
    
    public static List<Entity> cloneList(List<Entity> list) throws CloneNotSupportedException {
        List<Entity> clone = new ArrayList<Entity>(list.size());
        for (Entity item : list) clone.add((Entity) item.clone());
        return clone;
    }
    
    public static void printList(List<Entity> entities) {
    	for(Entity entity : entities) {
    		entity.print();
    	}
    }
    
    public static void moveTo(Point point, int power) {
        System.out.print("MOVE ");
        System.out.print((int)point.getX());
        System.out.print(" ");
        System.out.print((int)point.getY());
        System.out.print(" ");
        System.out.println(power);
    }
    
    public static void throwTo(Point point, int power) {
        System.out.print("THROW ");
        System.out.print((int)point.getX());
        System.out.print(" ");
        System.out.print((int)point.getY());
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
	    	
	    	snaffles1[0] = wizards.get(0).searchNearestEntity(snaffles);
	    	//snaffles1[1] = wizards.get(1).searchNearestSnaffleExcept(snaffles, snaffles1[0].getId());
	    	snaffles1[1] = wizards.get(1).searchNearestEntity(snaffles);
	    	
	    	double totalDistanceSquare1 = wizards.get(0).computeDistanceSquare(snaffles1[0]);
	    	totalDistanceSquare1+= wizards.get(1).computeDistanceSquare(snaffles1[1]);
	    	
	    	Entity[] snaffles2 = new Entity[2];
	    	
	    	snaffles2[1] = wizards.get(1).searchNearestEntity(snaffles);
	    	//snaffles2[0] = wizards.get(0).searchNearestSnaffleExcept(snaffles, snaffles2[1].getId());
	    	snaffles2[0] = wizards.get(0).searchNearestEntity(snaffles);
	    	
	    	double totalDistanceSquare2 = wizards.get(0).computeDistanceSquare(snaffles2[0]);
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
    	
    	double Ax = A.getX();
    	double Ay = A.getY();
    	double Bx = B.getX();
    	double By = B.getY();
    	double Cx = C.getX();
    	double Cy = C.getY();

    	
    	// compute the euclidean distance between A and B
    	double LAB = (double) Math.sqrt( (Bx-Ax)*(Bx-Ax)+(By-Ay)*(By-Ay) );

    	// compute the direction vector D from A to B
    	double Dx = (Bx-Ax)/LAB;
    	double Dy = (By-Ay)/LAB;

    	// Now the line equation is x = Dx*t + Ax, y = Dy*t + Ay with 0 <= t <= 1.

    	// compute the value t of the closest point to the circle center (Cx, Cy)
    	double t = Dx*(Cx-Ax) + Dy*(Cy-Ay); 

    	// This is the projection of C on the line from A to B.

    	// compute the coordinates of the point E on line and closest to C
    	double Ex = t*Dx+Ax;
    	double Ey = t*Dy+Ay;

    	// compute the euclidean distance from E to C
    	double LEC = (float) Math.sqrt( (Ex-Cx)*(Ex-Cx)+(Ey-Cy)*(Ey-Cy) );

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
				double velocity1 = Math.abs(o1.getVx());
				double velocity2 = Math.abs( o2.getVx());
				
				
				return Double.compare(velocity2, velocity1);
			}
        });
    	
    	return snafflesGoingToGoal;
    }
    
    
    static void play(List<Entity> wizardsAndBludgers) {
    	
    	
        // This tracks the time during the turn. The goal is to reach 1.0
        double t = 0.0;

        while (t < 1.0) {
        	
        	
            Collision firstCollision = null;

            // We look for all the collisions that are going to occur during the turn
            for (int i = 0; i < wizardsAndBludgers.size(); ++i) {
                // Collision with another pod?
                for (int j = i + 1; j < wizardsAndBludgers.size(); ++j) {
                    Collision col = wizardsAndBludgers.get(i).collision(wizardsAndBludgers.get(j));
                    
                    // If the collision occurs earlier than the one we currently have we keep it
                    if (col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
                    	
                    	int typeA = col.entityA.getType();
                    	int typeB = col.entityB.getType();
                    	
                    	//If there is a wizard (0) and a snaffle (1)
                    	if(( typeA == 1 &&  typeB == 0 )||( typeA == 0 &&  typeB == 1 )) {
                    		//System.err.println("*********************************************************");
                    		Entity wizard;
                    		Snaffle snaffle;
                    		if(typeA == 0) {
                    			wizard = col.entityA;
                    			snaffle = (Snaffle) col.entityB;
                    		} else {
                    			snaffle = (Snaffle) col.entityA;
                    			wizard = col.entityB;
                    		}
                    		
                    		//If this snaffle is not catched, the wizard catch it
                    		if(!snaffle.isCatched() && wizard.getState() == 0) {
                    			System.err.println("Snaffle " + snaffle.getId() + " catched by " + wizard.getId());
                    			snaffle.setWizard(wizard);
                    			wizard.setSnaffleCarried(snaffle);
                    			wizard.setState(1);
                    		}
                    		//FIXME: what happens when snaffle it wizard which is carrying an other snaffle ?
                    	} else {
                            firstCollision = col;
                    	}
                        
                    }
                }
                
                Collision col = wizardsAndBludgers.get(i).collisionWithWall();
                // If the collision occurs earlier than the one we currently have we keep it
                if (col != null && col.t + t < 1.0 && (firstCollision == null || col.t < firstCollision.t)) {
                    firstCollision = col;
                    
                }
                
            }

            if (firstCollision == null) {
                // No collision, we can move the pods until the end of the turn
                for (int i = 0; i < wizardsAndBludgers.size(); ++i) {
                	//wizards.get(i).printAll();
                    wizardsAndBludgers.get(i).move(1.0 - t);
                    
                }

                // End of the turn
                t = 1.0;
            } else {
                // Move the pods to reach the time `t` of the collision
                for (int i = 0; i < wizardsAndBludgers.size(); ++i) {
                    wizardsAndBludgers.get(i).move(firstCollision.t);
                }

                // Play out the collision
                firstCollision.entityA.bounce(firstCollision.entityB);
                
                //Change target of the bludger
                //TODO: WARNING : what happens if bludger touch 2 wizards in the same tour?
                if(firstCollision.entityA.getState() == 2) {
                	if(firstCollision.entityB.getState() == 1) {
                		Bludger bludger = (Bludger) firstCollision.entityA; 
                		bludger.setLastEntityId(firstCollision.entityB.id);
                	}
                } else {
                	if(firstCollision.entityB.getState() == 2) {
                		if(firstCollision.entityA.getState() == 1) {
                    		Bludger bludger = (Bludger) firstCollision.entityB; 
                    		bludger.setLastEntityId(firstCollision.entityA.id);
                		}
                	}
                }
                
                firstCollision.print();

                t += firstCollision.t;
            }
        }
        


        /*for (int i = 0; i < wizards.length; ++i) {
            wizards[i].end();
        }*/
    }

}