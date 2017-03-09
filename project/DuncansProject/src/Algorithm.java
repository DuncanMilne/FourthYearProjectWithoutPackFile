import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import gurobi.GRBException;

public class Algorithm implements Cloneable {

	protected ArrayList<Project> projects;
	protected ArrayList<Lecturer> lecturers;
	protected ArrayList<Student> assignedStudents;
	protected ArrayList<Student> unassigned;
	protected ArrayList<Student> projectlessStudents;
	protected StabilityChecker s = new StabilityChecker(this);
	protected Project emptyProject;
	protected ArrayList<Student> untouchedStudents;
	protected int instances;
	
	ArrayList<Student> applyingStudent;
	ArrayList<Project> applyingProject;

	public Algorithm() {
		projects = new ArrayList<Project>();
		lecturers = new ArrayList<Lecturer>();
		assignedStudents = new ArrayList<Student>();
		unassigned = new ArrayList<Student>();
		emptyProject = new Project("empty");
		projectlessStudents = new ArrayList<Student>();
		untouchedStudents = new ArrayList<Student>();
	}
	 
	public void writeToFile(String filename) {
		  try{
			    PrintWriter writer = new PrintWriter(filename, "UTF-8");
			    writer.println(projects.size() + " " + unassigned.size() + " " + lecturers.size());
			    for(Project p : projects) {
			    	writer.println(p.name + " " + p.capacity);
			    }
			    for (Student s : unassigned) {
			    	String prefListString = "";
			    	for (Project p : s.preferenceList) {
			    		prefListString += p.name + " ";
			    	}
			    	writer.println(s.name + ": " + prefListString);
			    }
			    for (Lecturer l : lecturers) {
			    	String projectListString = "";
			    	for (Project p : l.projects) {
			    		projectListString += p.name + " ";
			    	}
			    	writer.println(l.name + ": " + l.capacity + ": " + projectListString);
			    }
			    writer.close();
		  } catch (IOException e) {
			   // do something
		  }
	}
		  


	public void printInstance(int constraint) {
		
		this.printProjects();

		this.printStudents();

		this.printLecturers();
		
		if (constraint == 0) { 
			this.printMatching();
		} else  {
			this.printConstraintMatching();
		}
	}

	void printProjects(){
		System.out.println("PRINTING PROJECTS");
		System.out.println();
		ArrayList<Project> toPrint = projects;
		for (Project p: toPrint) {
			System.out.println(p.name + " " + p.capacity);
		}
		System.out.println();
	}

	void printStudents() {
		System.out.println("PRINTING STUDENTS");
		for (Student st: untouchedStudents) {
			System.out.print(st.name + " : ");
			for (Project p: st.preferenceList) {
				System.out.print(p.name + " ");
			}
			System.out.println("");
		}
	}

	void printLecturers() {
		System.out.println("PRINTING LECTURERS");
		ArrayList<Lecturer> toPrint = lecturers;
		for (Lecturer l: toPrint) {
			System.out.print(l.name + " : " + l.capacity + " : ");
			for (Project p: l.projects) {
				System.out.print(p.name + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	void printMatching() {
		System.out.println("PRINTING MATCHING");
		for (Student s:assignedStudents) {
			System.out.println(s.name + " " + s.proj.name);
		}
		System.out.println(assignedStudents.size() + " students were assigned a project");
        
        
	}
	
	void printConstraintMatching() {
		System.out.println("PRINTING CONSTRAINT MATCHING");
		
		int countOfMatched = 0;
		for (Student s:assignedStudents) {
			if (s.proj != null) {
				if (s.proj != emptyProject) {
					countOfMatched++;
					System.out.println(s.name + " " + s.proj.name);
				}
			}
		}
		

		System.out.println(countOfMatched + " students were matched");
	}

	void removeStudentFromArrayList(Lecturer firstProjectsLecturer,Project worstNonEmptyProject) {
		Random random = new Random();
		Student removeStudent;
		if (worstNonEmptyProject.unpromoted.size() > 0) {

			int removeInt = random.nextInt((worstNonEmptyProject.unpromoted.size()));
			if (removeInt != 0) {
				removeInt--; // allows access to each student
			}
			// remove a random student from the lecturersWorstNonEmptyProject
			removeStudent = worstNonEmptyProject.unpromoted.get(removeInt);
			worstNonEmptyProject.unpromoted.remove(removeStudent);
		} else {
		 	 int removeInt = random.nextInt((worstNonEmptyProject.promoted.size()));
		 	 if (removeInt != 0) {
		 		 removeInt--; // allows access to each student
		 	 }
		 	 // remove a random student from the lecturersWorstNonEmptyProject
		 	removeStudent = worstNonEmptyProject.promoted.get(removeInt);
			worstNonEmptyProject.promoted.remove(removeStudent);
		}
		
		removeStudent.proj = null;

		removeStudent.preferenceList.set(removeStudent.preferenceList.indexOf(worstNonEmptyProject), emptyProject);

		removeStudent.findNextFavouriteProject(this);

		if (removeStudent.rankingListTracker != -1){	//if they don't only have rejected projects
			unassigned.add(removeStudent);
		}

		assignedStudents.remove(removeStudent);
		firstProjectsLecturer.assigned--;
	}

	protected void spaPApproxPromotion(){} //#TODO work out how to make these abstract?

	protected void assignProjectsToStudents() {}
	
	protected void printMatchingOutput(int avg, int max, int min) {
		System.out.println("Average matching size was " + avg);
		System.out.println("Maximum matching size was " + max);
		System.out.println("Minimum matching size was " + min);
	}

	public void assignConstraints(Algorithm a) throws GRBException {
		
	}
	
}
