import java.util.ArrayList;
import gurobi.*;

public class Student {


	ArrayList<Project> preferenceList;
	
	// untouchedPreferenceList is used to print out instances after the execution of the program has completed.
	// This is useful for testing, as we may only want to print out instances that perform in certain ways, for example one which produces a blocking coalition.
	// The printed out instance can then be used for debugging purposes.
	ArrayList<Project> untouchedPreferenceList;
	
	// The students currently assigned project
	Project proj;
	
	// A list of Gurobi variables, one is added for each project in the students preference list.
	// In the IP programming model, these are used to calculate the optimal solution, if a gurobi variable belonging to a student is one, they are assigned the related project.
	ArrayList<GRBVar> grbvars;
	
	 // create envy list. This is a list pertaining to other students and whether or not this student envies them.
	ArrayList<GRBVar> envyList;

	// Used to find index of project in students preference list
	// i.e. index 0 contains preference value for project 0 for student
	int[] rankingList;

	// tracks current best project or assigned project if student is assigned in text input file
	int rankingListTracker;

	// The name of the student
	String name;

	// applicable to Spa P Approx Promotion only
	boolean promoted;

	
	public Student(String name) {
		// Every student initially prefers the first project in their preference list.
		rankingListTracker = 0;
		this.name = name;
		promoted = false;
		preferenceList = new ArrayList<Project>();
		untouchedPreferenceList = new ArrayList<Project>();
		grbvars = new ArrayList<GRBVar>();
		envyList = new ArrayList<GRBVar>();
	}

	public void promote() {
		// Effectively "promotes a student", allowing them to attempt to apply to each project in their preference list again. Only relevant for Spa-p-approx-promotion
		promoted = true;
		preferenceList = new ArrayList<Project>(untouchedPreferenceList);
		rankingListTracker = 0;
	}
	
	protected void findNextFavouriteProject(Algorithm a) {
		int max = -1;

		// iterates over students full ranking list
		for (int k = 0; k < rankingList.length; k++) {

			// found potential next favourite project
			if (preferenceList.get(k) != a.emptyProject){

				// if previous contender has been found
				if (max !=-1){

					// compare current with max
 					if (rankingList[k] < rankingList[max]) {
						max = k;
					}
				} else { // no contender found, this must be favourite
					max = k;
				}
			}
		}
		// if we didn't find a contender
		if (max == -1) {

			// student is now projectless
			a.unassigned.remove(this);
			a.projectlessStudents.add(this);
			rankingListTracker = -1;
		} else {

			// set students favourite project tracker to max
			rankingListTracker = max;
		}
	}

}
