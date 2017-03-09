import java.util.*;

import gurobi.GRBException;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

  static Algorithm algorithm;

  public static void main(String[] args) {
	  mygooey.main(args);
  }

  Algorithm instanceGenerator(String fileName) {
    Algorithm algorithm1 = new Algorithm();
    try {
    	
      BufferedReader br = new BufferedReader(new FileReader(fileName));

      String[] splitted = br.readLine().split(" ");

      int numProjects = Integer.parseInt(splitted[0]);

      int numStudents = Integer.parseInt(splitted[1]);

      int numLecturers = Integer.parseInt(splitted[2]);

      Student currentStudent;

      Student untouchedStudent;

      // Do projects first, track their lecturer and assign at the end
      Project currentProject;

      for (int i = 0; i < numProjects; i++) {
        splitted = br.readLine().split(" ");
        currentProject = new Project(splitted[0], Integer.parseInt(splitted[1]));
        algorithm1.projects.add(currentProject);
      }

      // now create students
      for(int i = 0; i < numStudents; i++) {

        splitted = br.readLine().split(" "); // get each student
        currentStudent = new Student(splitted[0].substring(0, splitted[0].length()-1));
        algorithm1.unassigned.add(currentStudent); // creates student with new name
        untouchedStudent =  new Student(splitted[0].substring(0, splitted[0].length()-1));
        algorithm1.untouchedStudents.add(untouchedStudent); // creates student with new name

        for (int j = 1; j < splitted.length; j++) {
          currentStudent.preferenceList.add(algorithm1.projects.get(Integer.parseInt(splitted[j])));
          untouchedStudent.preferenceList.add(algorithm1.projects.get(Integer.parseInt(splitted[j])));
        }

        currentStudent.rankingList = new int[currentStudent.preferenceList.size()];

	    for (int k = 0; k < currentStudent.preferenceList.size(); k++) {
			  currentStudent.rankingList[k] = k;  // Initially set rankings so index 0 is favourite, 1 is second favourite etc..
		 }

		currentStudent.untouchedPreferenceList = new ArrayList<Project>(currentStudent.preferenceList);
		untouchedStudent.untouchedPreferenceList = new ArrayList<Project>(untouchedStudent.preferenceList);
		   
      }

      Lecturer currentLecturer;
      // now create lecturers

      for (int i = 0; i < numLecturers; i++) {

        splitted = br.readLine().split(" ");
        currentLecturer = new Lecturer(splitted[0].substring(0, splitted[0].length()-1), Integer.parseInt(splitted[1].substring(0, splitted[1].length()-1)));
        algorithm1.lecturers.add(currentLecturer);
        for (int j = 2; j < splitted.length; j++) {
          currentLecturer.projects.add(algorithm1.projects.get(Integer.parseInt(splitted[j])));
          algorithm1.projects.get(Integer.parseInt(splitted[j])).lecturer = currentLecturer;
        }
      }
      
      String line;

      // add matchings
      ArrayList<Student> toBeRemoved = new ArrayList<Student>();
      Project matchingProject;
      int projectNumber;

      // #TODO if there are matches edit currentstudents rankingList
      while ((line = br.readLine()) != null) {

        splitted = line.split(",");
        currentStudent = algorithm1.unassigned.get(Integer.parseInt(splitted[0].substring(1, splitted[0].length()-1)));
        toBeRemoved.add(currentStudent);
        projectNumber = Integer.parseInt(splitted[1].substring(0, splitted[0].length()-1));
        projectNumber = currentStudent.preferenceList.indexOf(algorithm1.projects.get(projectNumber)); // get index of project in students pref list
        matchingProject = algorithm1.projects.get(projectNumber);
        currentStudent.proj = matchingProject;
        currentStudent.rankingListTracker = projectNumber;    // for stability checking purposes
        matchingProject.unpromoted.add(currentStudent);
        matchingProject.lecturer.assigned++;
      }

      br.close();


      algorithm1.assignedStudents.addAll(toBeRemoved);
      algorithm1.unassigned.removeAll(toBeRemoved);

    } catch (Exception e) {
      System.out.println("Type of error: " + e.getClass().getName() + " Error message: " + e.getMessage());
    }
    algorithm = algorithm1;
    createRankingLists();
    return algorithm;
  }

  public void go(int[] arguments, int promotion) throws GRBException, CloneNotSupportedException {

	  int iterations = arguments[5];
	  int avgCardinalityApprox = 0;
	  long avgTimeApprox = 0;
	  long avgTimePromotion = 0;
	  int avgCardinalityPromotion = 0;
	  for (int i=0; i < iterations; i++) {
		  algorithm = new Algorithm();
	      populate(arguments); // args0 is number of students to generate
	      assignCapacity(arguments[3], arguments[4]);	//assigns capacity to the projects, args are lecturer capacity and project capacity
	      assignProjectsToLecturers(); // Associate project with a lecturer
	      
	      // need three algorithms here so we can use the same data set

	      if (promotion == 0) {
	          algorithm = new ApproxPromotion(algorithm);
	          algorithm.spaPApproxPromotion();
	          algorithm.s.blockingCoalitionDetector(algorithm.assignedStudents, algorithm.emptyProject);      
	          algorithm.s.checkAssignedStudentsForBlockingPairs(algorithm.assignedStudents);      
	          algorithm.s.checkUnassignedStudentsForBlockingPairs(algorithm.unassigned);
	          algorithm.printInstance(0);
	      } else if (promotion == 1){
	          algorithm = new Approx(algorithm);
	          algorithm.assignProjectsToStudents();
	          algorithm.s.blockingCoalitionDetector(algorithm.assignedStudents, algorithm.emptyProject);
	          algorithm.s.checkAssignedStudentsForBlockingPairs(algorithm.assignedStudents);      
	          algorithm.s.checkUnassignedStudentsForBlockingPairs(algorithm.unassigned);
	          algorithm.printInstance(0);
	      } else if (promotion == 2){
	    	  algorithm.writeToFile("the-instance.txt");
	    	  algorithm = new GurobiModel(algorithm);
	    	  algorithm.assignConstraints(algorithm);
	          algorithm.s.blockingCoalitionDetector(algorithm.untouchedStudents, algorithm.emptyProject);
	          algorithm.s.IProgrammingBlockingPairs(algorithm.assignedStudents);
	      } else if (promotion == 3) {
	    	  algorithm.writeToFile("the-instance.txt");
	    	  algorithm.writeToFile("n" + algorithm.unassigned.size() + "instance" + i + ".txt");
	    	  Algorithm algorithm1 = instanceGenerator("the-instance.txt");
	    	  ApproxPromotion approxPromotion = new ApproxPromotion(algorithm1);
	    	  Algorithm algorithm2 = instanceGenerator("the-instance.txt");
	    	  Approx approx = new Approx(algorithm2);
	    	  Date startDateForApprox = new java.util.Date();
	          approx.assignProjectsToStudents();
	    	  Date endDateForApprox = new java.util.Date();
	          approx.s.blockingCoalitionDetector(algorithm2.assignedStudents, algorithm2.emptyProject);
	    	  Date startDateForPromotion = new java.util.Date();
	          approxPromotion.spaPApproxPromotion();
	    	  Date endDateForPromotion = new java.util.Date();
	          approxPromotion.s.blockingCoalitionDetector(approxPromotion.assignedStudents, approxPromotion.emptyProject);
	          avgCardinalityApprox += approx.assignedStudents.size();
	          avgTimeApprox += endDateForApprox.getTime() - startDateForApprox.getTime();
	          avgCardinalityPromotion += approxPromotion.assignedStudents.size();
	          avgTimePromotion += endDateForPromotion.getTime() - startDateForPromotion.getTime();
	          if (i == iterations-1)
	        	  System.out.println("Average approx size " + avgCardinalityApprox/iterations + " which took " + (double) avgTimeApprox/iterations + " milliseconds\nAverage approxpromotion size " + avgCardinalityPromotion/iterations + " which took " + (double) avgTimePromotion/iterations + " milliseconds");
	      } else {
	    	  algorithm.writeToFile("the-instance.txt");
	    	  try {
	    	  Algorithm algorithm1 = instanceGenerator("the-instance.txt");
	    	  Algorithm algorithm2 = instanceGenerator("the-instance.txt");
	    	  Algorithm algorithm3 = instanceGenerator("the-instance.txt");
	    	  Approx approx = new Approx(algorithm1);
	    	  ApproxPromotion approxPromotion = new ApproxPromotion(algorithm2);
	    	  GurobiModel gurobiModel = new GurobiModel(algorithm3);
	    	  approx.assignProjectsToStudents();
	          approx.s.blockingCoalitionDetector(approx.assignedStudents, approx.emptyProject);
	    	  approxPromotion.spaPApproxPromotion();
	          approxPromotion.s.blockingCoalitionDetector(approxPromotion.assignedStudents, approxPromotion.emptyProject);
	    	  gurobiModel.assignConstraints(gurobiModel);
	          gurobiModel.s.IProgrammingBlockingPairs(algorithm.assignedStudents);
	    	  System.out.println("approx size " + approx.assignedStudents.size() + " approxpromotion size " + approxPromotion.assignedStudents.size() + " ip programming size: " + gurobiModel.sizeOfMatching());
	    	  if (gurobiModel.sizeOfMatching() < approxPromotion.assignedStudents.size() || gurobiModel.sizeOfMatching() < approx.assignedStudents.size()) {
	    		  System.out.println("ERROR ERROR APPROX/APPROXPROMOTION IS FINDING A MATCHING LARGER THAN OPTIMAL!!!!!");
	    		  //approx.printInstance(0);
	    		  //approxPromotion.printInstance(0);
	    		  //gurobiModel.printInstance(1);
	    		  System.exit(1);
	    	  }
	    	  if (((gurobiModel.sizeOfMatching()/3)*2) > approxPromotion.assignedStudents.size()){ 
	    		  System.out.println("ERROR ERROR APPROXPROMOTION FOUND INSTANCE WITH A SIZE LESS THAN 2/3 OF OPTIMAL MATCHING!!");
	    		  //gurobiModel.printInstance(1);
	    		  //approxPromotion.printInstance(0);
	    	  }
	    	  if (((gurobiModel.sizeOfMatching()/2) > approx.assignedStudents.size())){ 
	    		  System.out.println("ERROR ERROR APPROX FOUND INSTANCE WITH A SIZE LESS THAN 2/3 OF OPTIMAL MATCHING!!");
	    		  gurobiModel.printInstance(1);
	    		  //approx.printInstance(0);
	    	  }
	    	  } catch (NumberFormatException e) {
	    		  e.printStackTrace();
	    	  }
	    	  
	      }
	  }
  }
  
 
  static void assignCapacity(int lecturerCapacity, int projectCapacity) {

		Random random = new Random();

		for (int i = 0; i< projectCapacity; i++) {
			algorithm.projects.get(random.nextInt(algorithm.projects.size())).capacity++;
		}

		for (int i = 0; i< lecturerCapacity; i++) {
		    algorithm.lecturers.get(random.nextInt(algorithm.lecturers.size())).capacity++;
		}
	}

	static void populate(int[] args) {
		populateProjects(args[0]);
		populateStudents(args[1]);
		populateLecturers(args[2]);
	}

	static void populateProjects(int numberOfProjects) {
		for (int i = 0; i < numberOfProjects; i++){
			algorithm.projects.add(new Project(Integer.toString(i)));
		}
	}


	static void populateStudents(int numberOfStudents) {

		for (int i = 0; i < numberOfStudents; i++){
			algorithm.unassigned.add(new Student(Integer.toString(i)));
			algorithm.untouchedStudents.add(new Student(Integer.toString(i)));
		}

		// populates student preference lists
		double random;
		Random randomProjectIndex = new Random();;
		ArrayList<Project> duplicateList;
		int rPI;
		// need to re-add projects after a student has been assigned all his projects
		for (int j = 0; j <algorithm.unassigned.size(); j++){ //for each student
			duplicateList = new ArrayList<Project>(algorithm.projects);
			random = Math.random()*2;

			// need to ensure we havent removed last item from duplicate list
			for (int i = 0; i < (random + 1) && !duplicateList.isEmpty(); i++) {
				rPI = randomProjectIndex.nextInt(duplicateList.size());
				algorithm.unassigned.get(j).preferenceList.add(duplicateList.get(rPI));
				algorithm.untouchedStudents.get(j).preferenceList.add(duplicateList.get(rPI));
				algorithm.unassigned.get(j).untouchedPreferenceList.add(duplicateList.get(rPI));
				algorithm.untouchedStudents.get(j).untouchedPreferenceList.add(duplicateList.get(rPI));
				duplicateList.remove(rPI);
			}
			//algorithm.unassigned.get(j).untouchedPreferenceList = new ArrayList<Project>(algorithm.unassigned.get(j).preferenceList);

  		}
  	}

    static void populateLecturers(int numberOfLecturers) {
      for (int i = 0; i < numberOfLecturers; i++){
        algorithm.lecturers.add(new Lecturer(Integer.toString(i)));
      }
    }


  	// for each project: assign random lecturer to project and assign project to lecturer
  	static void assignProjectsToLecturers () {
  		ArrayList<Project> proj = new ArrayList<Project>(algorithm.projects);

  		// first assign each lecturer one project
  		Random randomProjectIndex = new Random();
  		for (int i = 0; i < algorithm.lecturers.size() && proj.size()>0; i++) {
  			int randomInt = randomProjectIndex.nextInt(proj.size());
  			algorithm.lecturers.get(i).projects.add(proj.get(randomInt));
  			proj.get(randomInt).lecturer = algorithm.lecturers.get(i);
  			proj.remove(randomInt);
  		}

  		//currently each lecturer will get 3 each because the project will simply be assigned to whoevers left
  		// until we run out of projects

  		Project chosenProject;
  		Lecturer chosenLecturer;
  		while (proj.size()>0) {
  			int randomProjInt = randomProjectIndex.nextInt(proj.size());
  			int randomLectInt = randomProjectIndex.nextInt(algorithm.lecturers.size());
  			chosenProject = proj.get(randomProjInt);
  			chosenLecturer = algorithm.lecturers.get(randomLectInt);
  			chosenLecturer.projects.add(chosenProject);
  			chosenProject.lecturer = chosenLecturer;
  			proj.remove(randomProjInt);
  		}

      createRankingLists();
    }

    static void createRankingLists() {

      for (Student s: algorithm.unassigned) {
        s.rankingList = new int[s.preferenceList.size()];
        for (int i = 0; i < s.rankingList.length; i++) {
          s.rankingList[i] = i;  // Initially set rankings so index 0 is favourite, 1 is second favourite etc..
        }
      }
      
      for (Student s: algorithm.assignedStudents) {
          s.rankingList = new int[s.preferenceList.size()];
          for (int i = 0; i < s.rankingList.length; i++) {
            s.rankingList[i] = i;  // Initially set rankings so index 0 is favourite, 1 is second favourite etc..
          }
      }
      
      for (Student s: algorithm.untouchedStudents) {
          s.rankingList = new int[s.preferenceList.size()];
          for (int i = 0; i < s.rankingList.length; i++) {
            s.rankingList[i] = i;  // Initially set rankings so index 0 is favourite, 1 is second favourite etc..
          }
      }
      
      for (Lecturer l: algorithm.lecturers) {
        l.rankingList = new int[l.projects.size()];
        for (int i = 0; i < l.rankingList.length; i++) {
          l.rankingList[i] = i;
        }
      }
      
    }
}
