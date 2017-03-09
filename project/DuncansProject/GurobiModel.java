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
    
	public GurobiModel() {
		super();
	}
	
	public void assignConstraints(Algorithm a) throws GRBException {
	    upperLowerConstraints(a);
	}
	
	/**
	    * <p>Adds upper and lower quota constraints to projects and lecturers, and student upper quota.</p>
	    */
	    private void upperLowerConstraints(Algorithm a) throws GRBException {
	        // ----------------------------------------------------------------------------------------
	        // each student is matched to 1 or less projects
	        for (int x = 0; x < a.untouchedStudents.size(); x++) {
	            // get linear expression for the sum of all variables for a student
	            GRBLinExpr sumVarsForStudent = new GRBLinExpr();
	            for (int y = 0; y < unitArray[x].length; y++) {
	                sumVarsForStudent.addTerm(1, unitArray[x][y].studentPrefVar);
	            }
	            // each student is matched to 1 or less projects
	            grbmodel.addConstr(sumVarsForStudent, GRB.LESS_EQUAL, 1.0, "ConstraintStudent" + x);
	        }

	        // ----------------------------------------------------------------------------------------
	        // for each project
	        for (int y = 0; y < numProjects; y++) {
	            // get linear expressions for the sum of variables for this project
	            ArrayList<GRBVar> projList = projectLists.get(y);
	            GRBLinExpr numStudentsForProj = new GRBLinExpr();
	            for (int p = 0; p < projList.size(); p++) {
	                numStudentsForProj.addTerm(1, projList.get(p));
	            }

	            // The number of students a project has must be greater than or equal to the lower quota
	            grbmodel.addConstr(numStudentsForProj, GRB.GREATER_EQUAL, (double) m.projectLowerQuotas[y], "ConstraintProjectLQ" + y);

	            // The number of students a project has must be less than or equal to the max capacity
	            grbmodel.addConstr(numStudentsForProj, GRB.LESS_EQUAL, (double) m.projUpperQuotas[y], "ConstraintProjectUQ" + y);
	            
	        }

	        // ----------------------------------------------------------------------------------------
	        // for each lecturer 
	        for (int z = 0; z < numLecturers; z++) {
	            // get a linear expression for the sum of variables for this lecturer
	            ArrayList<IP_model_unit> lecList = lecturerLists.get(z);
	            GRBLinExpr numStudentsForLect = new GRBLinExpr();
	            for (int var = 0; var < lecList.size(); var++) {  
	                numStudentsForLect.addTerm(1, lecList.get(var).studentPrefVar);
	            }

	            // The number of students a lecturer has must be greater than or equal to the lower quota
	            grbmodel.addConstr(numStudentsForLect, GRB.GREATER_EQUAL, (double) m.lecturerLowerQuotas[z], "ConstraintLecturerLQ" + z);

	            // The number of students a lecturer has must be less than or equal to the max capacity
	            grbmodel.addConstr(numStudentsForLect, GRB.LESS_EQUAL, (double) m.lecturerUpperQuotas[z], "ConstraintLecturerUQ" + z);
	        }
	    }


}
