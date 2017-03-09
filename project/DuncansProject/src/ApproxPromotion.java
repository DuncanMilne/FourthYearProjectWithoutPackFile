import java.util.ArrayList;
import java.util.Random;

public class ApproxPromotion extends Algorithm{

  public ApproxPromotion() {
    super();
  }

  public ApproxPromotion(Algorithm algorithm) {
	this.assignedStudents = new ArrayList<Student>(algorithm.assignedStudents);
	this.projects = new ArrayList<Project>(algorithm.projects);
	this.lecturers = new ArrayList<Lecturer>(algorithm.lecturers);
	this.unassigned = new ArrayList<Student>(algorithm.unassigned);
	this.emptyProject = new Project("empty");
	this.projectlessStudents = new ArrayList<Student>(algorithm.projectlessStudents);
	this.untouchedStudents = new ArrayList<Student>(algorithm.untouchedStudents);
	this.s = new StabilityChecker(this);
  }

protected void spaPApproxPromotion() {
    // while there exists an unassigned student that has a non empty list of is unpromoted.
    // Simply check when adding back to unassigned if their list is non empty or if they are unpromoted. If unpromoted, promote them and re-add all items to their list
    // otherwise add them to projectless

    Lecturer fPL; //first projects lecturer

    Student stud; // random student chosen from list of unassigned students

    Project firstProj; // the random students first project

    int currentIndex; // used to locate students favourite project

    boolean ignoreFunctionRunthrough; // boolean value determining whether or not the student was promoted on this pass through the while loop

    Project wNEP; // the lecturers worst non empty project

    Random randomStudent = new Random();  // used to chose a random student

    while (!unassigned.isEmpty()) {

      wNEP = emptyProject;

      stud = unassigned.get(randomStudent.nextInt(unassigned.size()));

      ignoreFunctionRunthrough = false;

      /* if stud has empty preference list and is not promoted then promote them.
       * Otherwise if student has empty preference list and is promoted, add them to a list of projectless students
      */

      if (stud.rankingListTracker == -1) {
        if (!stud.promoted){
          stud.promote();
        } else {
          unassigned.remove(stud);
          projectlessStudents.add(stud);
          ignoreFunctionRunthrough = true;
        }
      }


      if (!ignoreFunctionRunthrough) { // used to ignore function runthrough if the student was promoted

        // get the index of the students favourite "available" project
        currentIndex = stud.rankingListTracker;

        // get that project
        firstProj = stud.preferenceList.get(currentIndex);

        fPL = firstProj.lecturer;

        wNEP = fPL.worstNonEmptyProject(wNEP);


        // Checks to see if project is full OR lecturer is full and the favourite project is the lecturer's worst non empty project
        if (((firstProj.unpromoted.size() + firstProj.promoted.size()) == firstProj.capacity || (fPL.isFull() && wNEP == firstProj))){

          // if student is unpromoted or there is no unpromoted student assigned to firstProj
          if (!stud.promoted || firstProj.unpromoted.size()==0){
        	// reject student and find their next favourite project
            stud.preferenceList.set(currentIndex, emptyProject);
            findNextFavouriteProject(stud);

          } else {
            // get random unpromoted student from the project's currently assigned students
            Student removeStudent = firstProj.unpromoted.get(randomStudent.nextInt(firstProj.unpromoted.size()));
            firstProj.unpromoted.remove(removeStudent);
            assignedStudents.remove(removeStudent);
            unassigned.add(removeStudent);
            fPL.assigned--;
            // set the project is the remove students list to be essentially -1
            removeStudent.preferenceList.set(removeStudent.rankingListTracker,emptyProject);
            findNextFavouriteProject(removeStudent);

            // add the student to the list of promoted students assigned to this project
            
            assignStudentToProj(stud, firstProj, fPL, wNEP);
          }
        } else if (fPL.isFull() && fPL.rankingList[fPL.projects.indexOf(wNEP)] < fPL.rankingList[fPL.projects.indexOf(firstProj)]) {
            	stud.preferenceList.set(currentIndex, emptyProject);
                findNextFavouriteProject(stud);
              
        } else {
            assignStudentToProj(stud, firstProj, fPL, wNEP);
        }
      }
    }
  }

  void assignStudentToProj(Student stud, Project firstProj, Lecturer fPL, Project wNEP){
    stud.proj = firstProj;
    if (!stud.promoted)
    	firstProj.unpromoted.add(stud);
    else
    	firstProj.promoted.add(stud);
    assignedStudents.add(stud);
    unassigned.remove(stud);
    fPL.assigned++;
    wNEP = fPL.worstNonEmptyProject(wNEP);
    // if lecturer is oversubscribed
    if (fPL.assigned > fPL.capacity) {
      removeStudentFromArrayList(fPL, wNEP);
    }
  }
  

	protected void findNextFavouriteProject(Student currentStudent) {
		int max = -1;

		// iterates over students full ranking list
		for (int k = 0; k < currentStudent.rankingList.length; k++) {

			// found potential next favourite project
			if (currentStudent.preferenceList.get(k) != emptyProject){

				// if previous contender has been found
				if (max !=-1){

					// compare current with max
 					if (currentStudent.rankingList[k] < currentStudent.rankingList[max]) {
						max = k;
					}
				} else { // no contender found, this must be favourite
					max = k;
				}
			}
		}
		currentStudent.rankingListTracker = max;
	}

}
