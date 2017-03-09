import java.util.ArrayList;
public class Project {

	Lecturer lecturer;
	String name;

	ArrayList<Student> unpromoted;

	ArrayList<Student> promoted;

	int capacity;

	public Project(String name) {
		this.name = name;
		this.capacity = 1;
		unpromoted = new ArrayList<Student>();
		promoted = new ArrayList<Student>();
		//could make capacity random int between 1-3
	}

	public Project(String name, int capacity) {
		this.name = name;
		this.capacity = capacity;
		unpromoted = new ArrayList<Student>();
		promoted = new ArrayList<Student>();
	}
}
