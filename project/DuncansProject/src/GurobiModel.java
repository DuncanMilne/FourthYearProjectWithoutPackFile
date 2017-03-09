import java.util.ArrayList;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiModel extends Algorithm {

    /** <p>The gurobi environment.</p> */
    GRBEnv env;
    /** <p>The gurobi model.</p> */
    GRBModel grbmodel;

	ArrayList<GRBLinExpr> rhsvars = new ArrayList<GRBLinExpr>();
	ArrayList<GRBLinExpr> lhsvars = new ArrayList<GRBLinExpr>();
	ArrayList<GRBVar> UCPTracker = new ArrayList<GRBVar>();
	ArrayList<Integer> vtracker = new ArrayList<Integer>();
    boolean feasible = true;
	ArrayList<GRBVar> underCapacityProjects = new ArrayList<GRBVar>();
	ArrayList<GRBVar> underCapacityLecturers = new ArrayList<GRBVar>();
	ArrayList<GRBVar> delta = new ArrayList<GRBVar>();
	ArrayList<GRBVar> gammas = new ArrayList<GRBVar>();
	ArrayList<GRBLinExpr> toprint = new ArrayList<GRBLinExpr>();
	ArrayList<GRBLinExpr> aijktoprint = new ArrayList<GRBLinExpr>();
	ArrayList<GRBVar> printinga = new ArrayList<GRBVar>();
	ArrayList<Double> doubles = new ArrayList<Double>();
	
    public GurobiModel() {
		super();
        try {
			env = new GRBEnv();
	        grbmodel = new GRBModel(env);
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public GurobiModel(Algorithm algorithm) {
  		this.projects = new ArrayList<Project>(algorithm.projects);
  		this.lecturers = new ArrayList<Lecturer>(algorithm.lecturers);
  		this.emptyProject = new Project("empty");
  		this.assignedStudents = new ArrayList<Student>(algorithm.unassigned);
  		this.untouchedStudents = new ArrayList<Student>(algorithm.untouchedStudents);
        try {
			env = new GRBEnv();
	        grbmodel = new GRBModel(env);
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}
	
	public int sizeOfMatching() {
		int countOfMatched = 0;
		for (Student s:assignedStudents) {
			if (s.proj != null) {
				if (s.proj != emptyProject) {
					countOfMatched++;
				}
			}
		}
		return countOfMatched;
	}

	public void assignConstraints(Algorithm a) throws GRBException {

	    grbmodel.getEnv().set(GRB.IntParam.OutputFlag, 0);
	        
		upperLowerConstraints(a);
			
		addMaxSizeConstraint(a);
		
		blockingCoalitionConstraints(a);
		
		assign3aConstraints(a);

		assign3bConstraints(a);

		assign3cConstraints(a);
		
        grbmodel.optimize();
	   	 
        int status = grbmodel.get(GRB.IntAttr.Status);
        
        if (status != GRB.Status.OPTIMAL) {
            feasible = false;
            System.out.println("no solution found in the following instance:");
            a.printInstance(1);
        }
        else {
            setStudentAssignments(a);
            System.out.println("success");
        }
        // write model then dispose of model and environment
//        for (GRBVar v: printinga){
//        	System.out.println("a " + v.get(GRB.DoubleAttr.X));
////        }
//        for (Student s: a.assignedStudents){
//        	for (GRBVar v: s.grbvars){
//        		System.out.println(v.get(GRB.DoubleAttr.X));
//        	}
//        }
        for (GRBVar g: delta){
        	System.out.println(g.get(GRB.DoubleAttr.X));
        }
    	//System.out.println(aijktoprint.get(0).getVar(lk).get(GRB.DoubleAttr.X));
    	//System.out.println(aijktoprint.get(0).getValue());
        
        //m.setInfoString(infoString);
        //grbmodel.write("SPA_IP_HR.lp");
        grbmodel.dispose();
        env.dispose(); 
	}
	
	// this condition asserts that 4/4 conditions are not true, if they are that is bad so we stop that from happening
	private void assign3aConstraints(Algorithm a) throws GRBException {
		
		// first condition Si is unassigned or Si prefers Pj to M(Si)

		
		for (Project p: projects) {
				GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, p.name + " under capacity");
				v.set(GRB.DoubleAttr.Start, 0.0);
				underCapacityProjects.add(v); // Ei,i'
		}
		
		//need to make variables a b c and d for each student/project/lecturer combo. projects + lecturers will obv be matched

		for (Student s:assignedStudents){
			
			int i = 0; //tracks current project location in pref list
			
			for (Project p:s.preferenceList){
				
				GRBLinExpr Aijk = new GRBLinExpr();
				 	
				Aijk.addConstant(1.0);
				
				GRBLinExpr sumOf = new GRBLinExpr();
				
				// sumOf is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j ++) {
					sumOf.addTerm(1.0, s.grbvars.get(j));
				}
				
				Aijk.multAdd(-1.0, sumOf);
				
				GRBLinExpr BijkRHS = new GRBLinExpr();
				
				GRBLinExpr BijkLHS = new GRBLinExpr();
				
				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));
				
				BijkRHS.addConstant(p.capacity); // instead of dividing Eijk by capacity of project, times everything else by capacity of project
				
				// 1- (students assigned to projects/capacity of project
				GRBLinExpr Eijk = new GRBLinExpr();
				
				// Eijk is used to count how many students are subscribed to p
				for (Student t:assignedStudents) {
					if (t.preferenceList.contains(p)) {
						Eijk.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(p)));
					}
				}
					
				BijkRHS.multAdd(-1.0, Eijk);
					
				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");
				
				 
				GRBLinExpr Cijk = new GRBLinExpr();
				
				for (Project q:p.lecturer.projects) {
					if (s.preferenceList.contains(q) && p!=q) {
						Cijk.addTerm(1.0, s.grbvars.get(s.preferenceList.indexOf(q)));
					}
				}

				GRBLinExpr Dijk = new GRBLinExpr();
				
				for (int j = p.lecturer.projects.indexOf(p)+1; j < p.lecturer.projects.size(); j ++) {
					Project curr = p.lecturer.projects.get(j);
					if (s.preferenceList.contains(curr)) {
						Dijk.addTerm(1.0, s.grbvars.get(s.preferenceList.indexOf(curr)));
					}
				}

				
				GRBLinExpr threeA = new GRBLinExpr();
				
				threeA.multAdd(1.0, Aijk);
				
				threeA.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));
				
				threeA.multAdd(1.0, Cijk);
				
				threeA.multAdd(1.0, Dijk);
				
				grbmodel.addConstr(threeA, GRB.LESS_EQUAL, 3, "constraint 3a");
				i++;
			}
		}
	}
	
	// this condition asserts that 4/4 conditions are not true, if they are that is bad so we stop that from happening
	private void assign3bConstraints(Algorithm a) throws GRBException {
		
		
		for (Lecturer l: a.lecturers) {
				GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, l.name + " under capacity");
				v.set(GRB.DoubleAttr.Start, 0.0);	// probably pointless
				underCapacityLecturers.add(v); // Ei,i'
		}
		
		for (Student s:assignedStudents){

			int i = 0; //tracks current project location in pref list

			for (Project p:s.preferenceList){
				GRBLinExpr Aijk = new GRBLinExpr();

				Aijk.addConstant(1.0);

				GRBLinExpr sumOf = new GRBLinExpr();
				
				// Aijk is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j ++) {
					sumOf.addTerm(1.0, s.grbvars.get(j));
				}
				
				Aijk.multAdd(-1.0, sumOf);

				GRBLinExpr BijkRHS = new GRBLinExpr();
				
				GRBLinExpr BijkLHS = new GRBLinExpr();
				
				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));
				
				BijkRHS.addConstant(p.capacity); // instead of dividing Eijk by capacity of project, times everything else by capacity of project
				
				// 1- (students assigned to projects/capacity of project
				GRBLinExpr Eijk = new GRBLinExpr();
				
				// Eijk is used to count how many students are subscribed to p
				for (Student t:assignedStudents) {
					if (t.preferenceList.contains(p)) {
						Eijk.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(p)));
					}
				}
					
				BijkRHS.multAdd(-1.0, Eijk);
					
				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");

				GRBLinExpr Cijk = new GRBLinExpr();
				GRBLinExpr sumXij = new GRBLinExpr();
				
				for (Project q:p.lecturer.projects) {
					if (s.preferenceList.contains(q)) {
						sumXij.addTerm(1.0, s.grbvars.get(s.preferenceList.indexOf(q)));
					}
				}
				
				Cijk.addConstant(1.0);
				
				Cijk.multAdd(-1.0, sumXij);
				
				
				GRBLinExpr DijkLHS = new GRBLinExpr();
				
				DijkLHS.addTerm(p.lecturer.capacity, underCapacityLecturers.get(lecturers.indexOf(p.lecturer)));
				
				GRBLinExpr DijkRHS = new GRBLinExpr();
				
				DijkRHS.addConstant(p.lecturer.capacity);
				
				GRBLinExpr bracketedExpression = new GRBLinExpr();
				
				for (Student t: assignedStudents) {
					for (Project q: t.preferenceList) {
						if (q.lecturer == p.lecturer){
							bracketedExpression.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(q)));
						}
					}
				}
				
				DijkRHS.multAdd(-1.0, bracketedExpression);
				

				grbmodel.addConstr(DijkLHS, GRB.GREATER_EQUAL, DijkRHS, "constraintname");
				
				
				GRBLinExpr threeB = new GRBLinExpr();
				
				threeB.multAdd(1.0, Aijk);
				
				threeB.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));
				
				threeB.multAdd(1.0, Cijk);
				
				threeB.addTerm(1.0, underCapacityLecturers.get(lecturers.indexOf(p.lecturer)));

				grbmodel.addConstr(threeB, GRB.LESS_EQUAL, 3, "constraint 3b");

				i++;
			}		
		}
	}

	// this condition asserts that 4/4 conditions are not true, if they are that is bad so we stop that from happening
	private void assign3cConstraints(Algorithm a) throws GRBException {

		
//		for (Lecturer l: a.lecturers) {
//			GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, l.name + " full");
//			delta.add(v); // Ei, i'
//		}
////		
		GRBVar currentDelta;
		for (Lecturer l: a.lecturers) {
			currentDelta = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, l.name + " gamma");
			gammas.add(currentDelta); 
		}

		for (Student s:assignedStudents){

			int i = 0; //tracks current project location in pref list

			for (Project p:s.preferenceList){
				GRBLinExpr Aijk = new GRBLinExpr();

				Aijk.addConstant(1.0);

				GRBLinExpr sumOf = new GRBLinExpr();
				
				// sumOf is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j ++) {
					sumOf.addTerm(1.0, s.grbvars.get(j));
					/*if (s == a.assignedStudents.get(1)&& p == a.projects.get(2)){
						printinga.add(s.grbvars.get(j));
					}*/
					// should maybe print these as one should be 1 here
				}
				
				Aijk.multAdd(-1.0, sumOf);

				
				GRBLinExpr BijkRHS = new GRBLinExpr();

				GRBLinExpr BijkLHS = new GRBLinExpr();

				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));

				BijkRHS.addConstant(p.capacity); // instead of dividing Eijk by capacity of project, times everything else by capacity of project
				
				// 1- (students assigned to projects/capacity of project
				GRBLinExpr bracketRHS = new GRBLinExpr();
				
				// Eijk is used to count how many students are subscribed to p
				for (Student t:assignedStudents) {
					if (t.preferenceList.contains(p))
						bracketRHS.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(p)));
				}
					
				BijkRHS.multAdd(-1.0, bracketRHS);
					
				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");

				GRBLinExpr Cijk = new GRBLinExpr();
				GRBLinExpr sumXij = new GRBLinExpr();
				
				for (Project q:p.lecturer.projects) {
					if (s.preferenceList.contains(q)) {
						sumXij.addTerm(1.0, s.grbvars.get(s.preferenceList.indexOf(q)));
					}
				}
				
				Cijk.addConstant(1.0);
				
				Cijk.multAdd(-1.0, sumXij);

				GRBLinExpr DijkRHS = new GRBLinExpr();
				
				// delta is (v) in notes david gave to me
				
				GRBLinExpr sumOfRHS = new GRBLinExpr();
				// checks to see if lecturer prefers this project to their worst non empty project 
				for (int j = 0; j <= p.lecturer.projects.indexOf(p); j++) { // this should be for every proj they prefer to Pk
					for (Student t: assignedStudents) {
						if (t.preferenceList.contains(p.lecturer.projects.get(j))) {
							sumOfRHS.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(p.lecturer.projects.get(j)))); // get whether or not a student is assigned a project the lecturer prefers to their worstnonemptyproject
						}
					}
				}

				DijkRHS.multAdd(-1.0, sumOfRHS);

				DijkRHS.addConstant(p.lecturer.capacity);
				
				GRBLinExpr DijkLHS = new GRBLinExpr();
				
				GRBVar delta1 = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "22");
				DijkLHS.addTerm(p.lecturer.capacity, delta1);
				
				grbmodel.addConstr(DijkLHS, GRB.GREATER_EQUAL, DijkRHS, "lk if full");	
				
				GRBLinExpr EijkRHS = new GRBLinExpr();
				
				GRBLinExpr EijkBracketed = new GRBLinExpr();
				
				// Eijk is now (iv) in davids notes given to me
				// \gamma_k >= (\sum_{i’=1}^{n_1}\sum_{p_j’\in P_k} x_{i’,j’}) + 1 – d_k.  
				// gamma k is one if the lecturer is full
				for (int j = 0; j < p.lecturer.projects.size(); j++) { // how many students does the lecturer have
					for (Student t: assignedStudents) {
						if (t.preferenceList.contains(p.lecturer.projects.get(j))) {
							EijkBracketed.addTerm(1.0, t.grbvars.get(t.preferenceList.indexOf(p.lecturer.projects.get(j)))); 
						}
					}
				}

				EijkRHS.multAdd(1.0, EijkBracketed);

				EijkRHS.addConstant(1.0);
			
				EijkRHS.addConstant(-p.lecturer.capacity);

				grbmodel.addConstr(gammas.get(lecturers.indexOf(p.lecturer)), GRB.GREATER_EQUAL, EijkRHS, "gamma constraint");

				GRBLinExpr threeC = new GRBLinExpr();

				threeC.multAdd(1.0, Aijk);
				
				threeC.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));

				threeC.multAdd(1.0, Cijk);
				
				threeC.addTerm(1.0, delta1);

				threeC.addTerm(1.0, gammas.get(lecturers.indexOf(p.lecturer))); 

				grbmodel.addConstr(threeC, GRB.LESS_EQUAL, 4, "constraint 3c");
				
				i++;
			}
		}
	}
	
	/**
    * <p>Adds upper and lower quota constraints to projects and lecturers, and student upper quota.</p>
    */
    private void upperLowerConstraints(Algorithm a) throws GRBException {
        // ----------------------------------------------------------------------------------------
        // each student is matched to 1 or less projects
    	
    	// matching for test instance should be 
    	// s2 p0
    	// s1 p1
    	
        for (Student s:a.assignedStudents) {
            GRBLinExpr sumVarsForStudent = new GRBLinExpr();
            // could do for each student
        	for (Project p: s.preferenceList) {
        		GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "pref" + p.name);
        		s.grbvars.add(v);
        		sumVarsForStudent.addTerm(1, v);
        	}
            // each student is matched to 1 or less projects
            grbmodel.addConstr(sumVarsForStudent, GRB.LESS_EQUAL, 1.0, "ConstraintStudent " + s.name); 
        }
        
        GRBVar XX = grbmodel.addVar(1.0, 1.0, 0.0, GRB.BINARY, "test");
     
        a.assignedStudents.get(2).grbvars.set(0, XX);

        GRBVar XY = grbmodel.addVar(1.0, 1.0, 0.0, GRB.BINARY, "test");
        
        a.assignedStudents.get(1).grbvars.set(1, XY);        
        
        // ----------------------------------------------------------------------------------------
        // for each project 
        for (Project p: a.projects) {
            GRBLinExpr numStudentsForProj = new GRBLinExpr();
            // for every student, if this project is in their pref list, add term
            for (Student s: a.assignedStudents) {
            	if (s.preferenceList.contains(p)){
            		numStudentsForProj.addTerm(1,s.grbvars.get(s.preferenceList.indexOf(p)));
            	}
            }
            // The number of students a project has must be less than or equal to the max capacity
            grbmodel.addConstr(numStudentsForProj, GRB.LESS_EQUAL, (double) p.capacity, "ConstraintProjectUQ" + p.name);
        }
        
        
        // ----------------------------------------------------------------------------------------
        // for each lecturer
        int x = 0;
        for (Lecturer l: a.lecturers) {
            GRBLinExpr numStudentsForLect = new GRBLinExpr();    
            for (Student s:a.assignedStudents) {
            	for (Project p: s.preferenceList) {
            		if (l.projects.contains(p)){
                		numStudentsForLect.addTerm(1, s.grbvars.get(x));
            		}
            		x++;
            	}
           	 	x=0;
            }
            grbmodel.addConstr(numStudentsForLect, GRB.LESS_EQUAL, (double) l.capacity, "ConstraintLecturerUQ" + l.name);
        }
    }

    /**
     * <p>Optimises on the maximum size and adds relevant constraint.</p>
     */
     public void addMaxSizeConstraint(Algorithm a) throws GRBException {
     	 GRBLinExpr sumAllVariables = new GRBLinExpr();
          for (Student s: a.assignedStudents) {
         	 for (GRBVar var: s.grbvars)
                sumAllVariables.addTerm(1, var);
          }

          grbmodel.setObjective(sumAllVariables, GRB.MAXIMIZE);

     }

     private void blockingCoalitionConstraints(Algorithm a) throws GRBException {
    	 // First we create an envy graph
    	 
    	 for (Student i1:a.assignedStudents){
    		 for (Student i2: a.assignedStudents) {
    			 if (i1!=i2){
    				 GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, i1 + " envies " + i2.name + " if this is 1.0");  
    				 v.set(GRB.DoubleAttr.Start, 0.0); // Ei,i'
    				 UCPTracker.add(v);
    				 i1.envyList.add(v);
    				 for (Project j1:i1.preferenceList) {
    					 // for every project i1 prefers to j1
    					 for (int x = 0; x < i1.preferenceList.indexOf(j1); x++) {
        					 Project j2 = i1.preferenceList.get(x);
    						 if (i2.preferenceList.contains(j2)) { //if i2 likes project j2
    					     	 GRBLinExpr lhs = new GRBLinExpr();
    							 GRBLinExpr rhs = new GRBLinExpr();
    							 //System.out.println("i1 is " + i1.name + " i2 is " + i2.name + " j1 is " + j1.name + " j2 is " + j2.name);
    							 
    					     	 lhs.addConstant(1.0);
    					     	 lhs.addTerm(1, v);
    					     	 rhs.addTerm(1, i1.grbvars.get(i1.preferenceList.indexOf(j1)));
    					     	 rhs.addTerm(1, i2.grbvars.get(i2.preferenceList.indexOf(j2)));	
    					     	 grbmodel.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "creates envygraph"); // this will set v to be 1 for any student that envies any other
    					     	 lhsvars.add(lhs);
    					     	 lhsvars.add(rhs);
    					     }
    					 }
    				 }
    			 }
    		 }	
    	 }
    	 
    	 ArrayList<GRBVar> vertexLabels = new ArrayList<GRBVar>();
    	 
    	 for (int i = 0; i< a.assignedStudents.size(); i++) {
    		 GRBVar v = grbmodel.addVar(0.0, assignedStudents.size()-1, 0.0, GRB.INTEGER, "vertex label for " + i);
    		 vertexLabels.add(v);
    	 }
    	 
    	 int i1Index = 0;
    	 int i2Index = 0;
    	 
    	 // checks for topological ordering
    	 
    	 for (Student i1:a.assignedStudents) {
    		 for (Student i2:a.assignedStudents) { // can find vi' by getting grbvar at certain indexes
    			 if (i1 != i2){
    				 
	    			 GRBLinExpr lhs = new GRBLinExpr();
	    			 GRBLinExpr rhs = new GRBLinExpr();
	    			 
	    			 GRBLinExpr bracketedExpression = new GRBLinExpr();
	    			 bracketedExpression.addTerm(-1, i1.envyList.get(i2Index));
	    			 bracketedExpression.addConstant(1.0);
	    			 rhs.addTerm(1.0, vertexLabels.get(a.assignedStudents.indexOf(i2)));
	    			 rhs.multAdd(a.assignedStudents.size(), bracketedExpression);

	    			 lhs.addTerm(1.0, vertexLabels.get(i1Index));
	    			 lhs.addConstant(1.0);
	    			 grbmodel.addConstr(lhs, GRB.LESS_EQUAL, rhs, "myconstraint2");
	    			 i2Index++;
    			 }
    		 }
    		 i2Index = 0;
    		 i1Index++;
    	 }
     }
     
    // The assignments have already been chosen by the constraints, this is just setting them
    public void setStudentAssignments(Algorithm a) throws GRBException {
    	// ready to save the assigned students to the studentAssignments array in the model

        // set the student assignments
        for (int x = 0; x < a.assignedStudents.size(); x++) {
        	Student s = a.assignedStudents.get(x);
            int prefLength = s.preferenceList.size();
            boolean matched = false;
            // for every preference of current student
            for (int projInd = 0; projInd < prefLength; projInd++) {
                double resultPref = s.grbvars.get(projInd).get(GRB.DoubleAttr.X);
                if (resultPref > 0.5) {
                    s.proj = s.preferenceList.get(projInd); 
                    matched = true;
                    s.proj.unpromoted.add(s);
                    s.proj.lecturer.assigned++;
                }
            }
            if (!matched){
                s.proj = emptyProject;
            }
        }
    }
}