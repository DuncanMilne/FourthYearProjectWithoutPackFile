import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gurobi.GRBException;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class mygooey {

	protected Shell shell;
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	private Text text_5;
	private Text txtAlternatively;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			mygooey window = new mygooey();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(543, 308);
		shell.setText("SWT Application");

		Label lblNumberOfStudents = new Label(shell, SWT.NONE);
		lblNumberOfStudents.setBounds(30, 10, 144, 15);
		lblNumberOfStudents.setText("Number of Students");

		Label lblNumberOfProjects = new Label(shell, SWT.NONE);
		lblNumberOfProjects.setBounds(170, 10, 109, 15);
		lblNumberOfProjects.setText("Number of Projects");

		Label lblNumberOfLecturers = new Label(shell, SWT.NONE);
		lblNumberOfLecturers.setBounds(298, 10, 163, 15);
		lblNumberOfLecturers.setText("Number of Lecturers");

		Label lblAdditionalCapacityFor = new Label(shell, SWT.NONE);
		lblAdditionalCapacityFor.setBounds(21, 58, 190, 15);
		lblAdditionalCapacityFor.setText("Additional Capacity for Lecturers");

		Label lblAdditionalCapacityFor_1 = new Label(shell, SWT.NONE);
		lblAdditionalCapacityFor_1.setBounds(217, 58, 190, 15);
		lblAdditionalCapacityFor_1.setText("Additional Capacity for Projects");

		text = new Text(shell, SWT.BORDER);
		text.setBounds(45, 31, 76, 21);

		text_1 = new Text(shell, SWT.BORDER);
		text_1.setBounds(184, 31, 76, 21);

		text_2 = new Text(shell, SWT.BORDER);
		text_2.setBounds(320, 31, 76, 21);

		text_3 = new Text(shell, SWT.BORDER);
		text_3.setBounds(78, 79, 76, 21);

		text_4 = new Text(shell, SWT.BORDER);
		text_4.setBounds(261, 79, 76, 21);

		Button btnRunAlgorithm = new Button(shell, SWT.NONE);
		btnRunAlgorithm.setBounds(209, 234, 102, 25);
		btnRunAlgorithm.setText("Run Algorithm");

		Label lblHowManyTimes = new Label(shell, SWT.NONE);
		lblHowManyTimes.setBounds(10, 187, 285, 16);
		lblHowManyTimes.setText("How many times would you like the algorithm to run?");

		text_5 = new Text(shell, SWT.BORDER);
		text_5.setBounds(108, 209, 76, 21);

		Button btnSpapapprox = new Button(shell, SWT.RADIO);
		btnSpapapprox.setBounds(30, 110, 102, 16);
		btnSpapapprox.setText("SPA-P-APPROX");

		Button btnSpapapproxpromotion = new Button(shell, SWT.RADIO);
		btnSpapapproxpromotion.setBounds(30, 132, 185, 16);
		btnSpapapproxpromotion.setText("SPA-P-APPROX-PROMOTION");

		Button btnBoth = new Button(shell, SWT.RADIO);
		btnBoth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnBoth.setBounds(30, 154, 224, 16);
		btnBoth.setText("Compare approximation algorithms");
		
		Button btnIpProgrammingModel = new Button(shell, SWT.RADIO);
		btnIpProgrammingModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnIpProgrammingModel.setBounds(248, 110, 146, 16);
		btnIpProgrammingModel.setText("IP programming model");
		
		Button btnCompareThreeImplementations = new Button(shell, SWT.RADIO);
		btnCompareThreeImplementations.setBounds(248, 132, 196, 16);
		btnCompareThreeImplementations.setText("Compare Three Implementations");
		
		txtAlternatively = new Text(shell, SWT.BORDER);
		txtAlternatively.setBounds(365, 209, 76, 21);
		
		Label lblAlternativelyEnterAn = new Label(shell, SWT.NONE);
		lblAlternativelyEnterAn.setBounds(318, 187, 206, 15);
		lblAlternativelyEnterAn.setText("Alternatively, enter an input file name");

		btnRunAlgorithm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {

			  Main main = new Main();
			      
			  if (txtAlternatively.getText()!="") {
				  if (btnCompareThreeImplementations.getSelection()) {
					  try {
					  
				    	  Algorithm algorithm1 = main.instanceGenerator(txtAlternatively.getText());
				    	  Algorithm algorithm2 = main.instanceGenerator(txtAlternatively.getText());
				    	  Algorithm algorithm3 = main.instanceGenerator(txtAlternatively.getText());
				    	  Approx approx = new Approx(algorithm1);
				    	  ApproxPromotion approxPromotion = new ApproxPromotion(algorithm2);
				    	  GurobiModel gurobiModel = new GurobiModel(algorithm3);
				    	  approx.assignProjectsToStudents();
				          approx.s.blockingCoalitionDetector(approx.assignedStudents, approx.emptyProject);
				    	  approxPromotion.spaPApproxPromotion();
				          approxPromotion.s.blockingCoalitionDetector(approxPromotion.assignedStudents, approxPromotion.emptyProject);
				    	  gurobiModel.assignConstraints(gurobiModel);
						  gurobiModel.s.IProgrammingBlockingPairs(gurobiModel.assignedStudents);
				    	  System.out.println("approx size " + approx.assignedStudents.size() + " approxpromotion size " + approxPromotion.assignedStudents.size() + " ip programming size: " + gurobiModel.sizeOfMatching());
				    	  if (gurobiModel.sizeOfMatching() < approxPromotion.assignedStudents.size() || gurobiModel.sizeOfMatching() < approx.assignedStudents.size()) {
				    		  System.out.println("ERROR ERROR APPROX/APPROXPROMOTION IS FINDING A MATCHING LARGER THAN OPTIMAL!!!!!");
				    		  approx.printInstance(0);
				    		  approxPromotion.printInstance(0);
				    		  gurobiModel.printInstance(1);
				    		  System.exit(1);
				    	  }
			    	  } catch (NumberFormatException | GRBException e1) {
			    		  e1.printStackTrace();
			    	  }
				  } else if (btnBoth.getSelection()) {
					  Algorithm algorithm1 = main.instanceGenerator(txtAlternatively.getText());
			    	  Algorithm algorithm2 = main.instanceGenerator(txtAlternatively.getText());
			    	  Approx approx = new Approx(algorithm1);
			    	  ApproxPromotion approxPromotion = new ApproxPromotion(algorithm2);
			    	  approx.assignProjectsToStudents();
			          approx.s.blockingCoalitionDetector(approx.assignedStudents, approx.emptyProject);
			    	  approxPromotion.spaPApproxPromotion();
			          approxPromotion.s.blockingCoalitionDetector(approxPromotion.assignedStudents, approxPromotion.emptyProject);
			    	  System.out.println("approx size " + approx.assignedStudents.size() + " approxpromotion size " + approxPromotion.assignedStudents.size());
				  } else if (btnSpapapproxpromotion.getSelection()) {
					  Algorithm algorithm1 = main.instanceGenerator(txtAlternatively.getText());
			    	  ApproxPromotion approxPromotion = new ApproxPromotion(algorithm1);
			    	  approxPromotion.spaPApproxPromotion();
			          approxPromotion.s.blockingCoalitionDetector(approxPromotion.assignedStudents, approxPromotion.emptyProject);
			          approxPromotion.printInstance(0);
				  } else if (btnSpapapprox.getSelection()) {
					  Algorithm algorithm1 = main.instanceGenerator(txtAlternatively.getText());
			    	  Approx approx = new Approx(algorithm1);
			    	  approx.assignProjectsToStudents();
			          approx.s.blockingCoalitionDetector(approx.assignedStudents, approx.emptyProject);
			          approx.printInstance(0);
				  } else if (btnIpProgrammingModel.getSelection()) {
					  Algorithm algorithm3 = main.instanceGenerator(txtAlternatively.getText());
			    	  GurobiModel gurobiModel = new GurobiModel(algorithm3);
			    	  try {
						gurobiModel.assignConstraints(gurobiModel);
						gurobiModel.s.IProgrammingBlockingPairs(gurobiModel.assignedStudents);
						gurobiModel.printInstance(1);
					} catch (GRBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				  }
			  
				  					
				
			  } else {
		      int numberOfStudents = Integer.parseInt(text.getText());
		      
		      int numberOfProjects = Integer.parseInt(text_1.getText());

		      int numberOfLecturers = Integer.parseInt(text_2.getText());

		      int lecturerCapacity = Integer.parseInt(text_3.getText());

		      int projectCapacity = Integer.parseInt(text_4.getText());

		      int numberOfInstances = Integer.parseInt(text_5.getText());

		      int[] arguments = new int[] {numberOfProjects, numberOfStudents, numberOfLecturers, lecturerCapacity, projectCapacity, numberOfInstances};


	          try {
			      if (btnBoth.getSelection()){
			          main.go(arguments, 3);
				  } else if (btnSpapapproxpromotion.getSelection()) {
			          main.go(arguments, 0);
				  } else if (btnSpapapprox.getSelection()) {
			          main.go(arguments, 1);
				  } else if (btnIpProgrammingModel.getSelection()){
					  main.go(arguments, 2);
				  } else if (btnCompareThreeImplementations.getSelection()) {
					  main.go(arguments, 4);
				  } else {
					  System.out.println("Please select which algorithm you would like to run");
				  }
			  } catch (GRBException e1) {
				  // TODO Auto-generated catch block
				  e1.printStackTrace();
			  } catch (CloneNotSupportedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
		  }
		});
}
}
