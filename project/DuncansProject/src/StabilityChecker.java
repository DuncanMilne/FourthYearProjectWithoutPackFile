import java.util.ArrayList;

public class StabilityChecker {

  Algorithm algorithm;

  public StabilityChecker(Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  protected void blockingCoalitionDetector(ArrayList<Student> assignedStudents, Project emptyProject) {
    Digraph digraph = new Digraph();
    for (Student s:assignedStudents) {
      digraph.add(s); // add all students as nodes
    }
    for (Student s:assignedStudents) { // for every student add edges to other students who have a preferable project
      for (int p = 0; p < s.rankingList.length; p++) { // for every project they have
          if (s.rankingList[p] < s.untouchedPreferenceList.indexOf(s.proj)) { // If student prefers this project to their current project
            for (Student x:assignedStudents) { // make digraph edge from current student to all currently assigned to preferred project
              if (x.proj == s.untouchedPreferenceList.get(p)) {
            	  digraph.add(s, x);
              }
            }
        }
      }
    }
    
    // If a blocking coalition is found, print the instance and halt execution
    if (!digraph.isDag()) {
      System.out.println("The graph is not a dag");
      algorithm.printInstance(0);
      System.exit(0);
    }
  }

  void checkAssignedStudentsForBlockingPairs(ArrayList<Student> assignedStudents){
    
	Project currentProj;
    Project lecturersWorstNonEmptyProject=null;
    
    int rLT; // finds rating of current project
    
    for (Student s:assignedStudents) {
      rLT = s.preferenceList.indexOf(s.proj);
      for (int p = 0; p < s.rankingList.length; p++) {
        if (s.rankingList[p] < rLT &&  s.preferenceList.indexOf(s.proj) != p) { //finds all preferred projects by student
          currentProj = s.untouchedPreferenceList.get(p);
          if (currentProj.capacity != (currentProj.unpromoted.size()+currentProj.promoted.size())){
        	  System.out.println("test");
            if (currentProj.lecturer.capacity == currentProj.lecturer.assigned) {
              //currently just use location in lecturers list of projects to determine which they prefers #TODO FIX
              lecturersWorstNonEmptyProject = currentProj.lecturer.worstNonEmptyProject(currentProj);
              if (currentProj.lecturer.projects.indexOf(currentProj) < currentProj.lecturer.projects.indexOf(lecturersWorstNonEmptyProject)) {
                System.out.println("ERROR: Assigned student with full teacher who prefers this project");
                algorithm.printInstance(0);
              }
            } else {
              System.out.println("ERROR: Assigned student with under capacity teacher");
              algorithm.printInstance(0);

            }
          }

        }
      }
    }
  }


	void checkUnassignedStudentsForBlockingPairs(ArrayList<Student> unassignedStudents){
	  Project currentProj;
	  Project lecturersWorstNonEmptyProject=null;

		for (Student s:unassignedStudents) {
			for (int p = 0; p<s.untouchedPreferenceList.size(); p++)
				if (s.untouchedPreferenceList.get(p) != algorithm.emptyProject) {
					currentProj = s.untouchedPreferenceList.get(p);
					if (currentProj.lecturer.capacity == currentProj.lecturer.assigned) {
						//currently just use location in lecturers list of projects to determine which they prefers #TODO FIX
						lecturersWorstNonEmptyProject = currentProj.lecturer.worstNonEmptyProject(currentProj);
						if (currentProj.lecturer.projects.indexOf(currentProj) < currentProj.lecturer.projects.indexOf(lecturersWorstNonEmptyProject)) {
							System.out.println("ERROR: unassigned student with full teacher who prefers this project");
							algorithm.printInstance(0);
						}
					} else {
						System.out.println("ERROR: unassigned student with under capacity teacher");
						algorithm.printInstance(0);
					}
					// check if lecturer prefers this project to any non empty proects or if lecturer is not full
				}
			}
  }
	
	void IProgrammingBlockingPairs(ArrayList<Student> students) {
		Project currentProj;
		//checks 3a
		for (Student s:students) {
			if (s.proj!=algorithm.emptyProject){
				for (int p = 0; p < s.untouchedPreferenceList.indexOf(s.proj); p++){
					currentProj = s.untouchedPreferenceList.get(p);
					if (currentProj.capacity > currentProj.unpromoted.size()){ // do we even use unpromoted in ip programming?? 
						if (s.proj.lecturer == currentProj.lecturer){		// if the lecturer supervises both projects, and prefers one that the student also prefers
							if (s.proj.lecturer.projects.indexOf(s.proj) > currentProj.lecturer.projects.indexOf(currentProj)){
								algorithm.printInstance(1);
								System.out.println("DOES NOT COMPUTE, blocking pair condition 3a"); // 3a fails
								System.exit(1);
							}
							if (s.proj.lecturer != currentProj.lecturer){
							}
						} else { 
							 // 3b
							if (currentProj.lecturer.assigned < currentProj.lecturer.capacity) {
								algorithm.printInstance(1);
								System.out.println("DOES NOT COMPUTE, blocking pair condition 3b"); // 3a fails
								System.out.println("lecturer "+ currentProj.lecturer.name);
								System.out.println("project "+ currentProj.name);
								System.out.println("student "+ s.name);
								System.exit(1);
							}
							// 3c
							if (currentProj.lecturer.assigned == currentProj.lecturer.capacity) {
								if (currentProj.lecturer.projects.indexOf(currentProj) < s.proj.lecturer.projects.indexOf(s.proj)) {
									algorithm.printInstance(1);
									System.out.println("DOES NOT COMPUTE, blocking pair condition 3c"); // 3c fails
									System.exit(1);
								}
							}
						}
					}
				}
			} else { // fill with 3b
				for (int p = 0; p <s.untouchedPreferenceList.indexOf(s.proj); p++) {
					currentProj = s.untouchedPreferenceList.get(p);
					if (currentProj.capacity > currentProj.unpromoted.size()){
						if (currentProj.lecturer.capacity > currentProj.lecturer.assigned) { //if lecturer is under subscribed
							algorithm.printInstance(1);
							System.out.println("DOES NOT COMPUTE, blocking pair condition 3b -- empty student");
						}
					} else {
						if (currentProj.lecturer.projects.indexOf(currentProj) < s.proj.lecturer.projects.indexOf(s.proj)) {
							algorithm.printInstance(1);
							System.out.println("DOES NOT COMPUTE, blocking pair condition 3c here"); // 3c fails
							System.exit(1);
						}
					}
				}
			}
		}
		
	}
}