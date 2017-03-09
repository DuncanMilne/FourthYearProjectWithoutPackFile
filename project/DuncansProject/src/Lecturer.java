import java.util.ArrayList;

public class Lecturer {

	String name;

	ArrayList<Project> projects = new ArrayList<Project>();

	int worstNonEmptyProject; // simply indexes the ranking list which gets from point in projects

	// Used to find index of project in students preference list
	// i.e. index 0 contains preference value for project 0 for student
	int[] rankingList;

	int capacity;

	int assigned;

	public Lecturer(String name) {
		this.name = name;
		this.projects = new ArrayList<Project>();
		this.capacity = 1;
		this.assigned = 0;
	}

	public Lecturer(String name, int capacity) {
		this.name = name;
		this.projects = new ArrayList<Project>();
		this.capacity = capacity;
		this.assigned = 0;
	}
	
	public boolean isFull(){
		return capacity == assigned;
	}
	
	// Returns lecturersWorstNonEmptyProject
	public Project worstNonEmptyProject(Project lecturersWorstNonEmptyProject) {
		boolean foundNonEmpty = false;
		// iterate from the end as the last entry will contain the worst project
		for (int i = projects.size()-1; i>-1; i--){

			// if project is not empty
			if (projects.get(i).unpromoted.size() + projects.get(i).promoted.size()>0){
					lecturersWorstNonEmptyProject = projects.get(i);
					i=-1;
					foundNonEmpty = true;
			}
		}
		if (foundNonEmpty == true) {
			return lecturersWorstNonEmptyProject;
		} else { 
			return projects.get(projects.size()-1); // we return their worst as opposed to returning their optimal, if optimal is the only option
		}
	}
	
}
