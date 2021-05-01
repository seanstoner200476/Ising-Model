import java.awt.Color;			// Color
import java.awt.Graphics;		// Graphical output
import java.util.Random;		// Random number
import java.lang.Math;			// Math operations
import java.io.File; 			// Import the File class
import java.io.FileOutputStream;
import java.io.IOException;  	// Import the IOException class to handle errors
import java.io.FileWriter;  	// Import the FileWriter class


/**
 * @author Sean Stoner
 * 
 *
 */
public class IsingModel {	
	//static drawing panel for use by all methods
	
	static DrawingPanel panel = new DrawingPanel(800, 800); 
	static Graphics g = panel.getGraphics();

	public static void main(String[] args) {
		// Adjustable parms
		int N = 150; 								// Array Size
		int update = 500;							// Used to control how often the graphic is updated when update mod pass = 0
		int pass = N*N*300;							// Number of passes to execute 
		double T = 1.5;								// Temperature in J/KbT
		double finalT = 2.62;							// final temp in J/Kb
		int steps = 50;								// number of steps between temp and finalTemp
		double P = .6;								// Probability of spin up or down between 0 and 1
		double J = 1.0;								// Strength of interaction between spins
		boolean drawModel = false;					// used to indicate if the model should be drawn during the sim or not.
		boolean singleOrMulti = true; 				// If false run single else run multi
		//fixed parms
		int model[][] =  new int[N+2][N+2];	 		// initialize the array	with 2 extra rows and columns for boarders
		double M = 0.0;								// Magnetization in u/N^2 between 0 and 1
		double HC = 0.0;							// Heat Capacity in J/Kb^2
		double resultsArray[][] = new double[steps + 1][3];

		//initial display window if active 		
		panel.setBackground(Color.GRAY);
		
		if(drawModel == false) panel.setVisible(false);		//Hide the graphic if not active
		
		//run for a single temperature or changing temperature
		if(singleOrMulti == false)	RunSingleT(model, N, update, pass, M, HC, T, P, J, drawModel);		
		else RunVarT(model, N, update, pass, M, HC, T, P, J, drawModel, steps, finalT, resultsArray);

	}
	
	public static void RunVarT(int model[][], int N, int update, int pass, 
			double M, double HC, double T, double P, double J, boolean drawModel, int steps, double finalT,
			double resultsArray[][]){
		
		/*
		 * INPUTS 
		 * 		N 								// Array Size
		 *		update 							// Used to control how often the graphic is updated when update mod pass = 0
		 *		pass							// Number of passes to execute
		 *		M 								// Magnetization
		 *		HC								// Heat Capacity in J/Kb^2
		 *		T 								// Temperature in J/KbT
		 *		P 								// Probability of spin up between 0 and 1
		 *		J								// Strength of interaction between spins
		 *		model[][]	 					// initialize the array	with 2 extra rows and columns for boarders
		 *		drawModel						// used to indicate if the model should be drawn during the sim or not.
		 *		steps							// number of steps between temp and finalTemp
		 *		finalT							// final temp in J/Kb
		 *
		 *	This method runs the at several temperatures from T to finalT with 
		 */
		
		int resultsIndex = 0;	// index element to place results in an array
		
		//Make a file for output
	    try {
	        File myObj = new File("Ising.txt");
	        if (myObj.createNewFile()) {
	          System.out.println("File created: " + myObj.getName());
	        }
	    }
	    catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	    }
	        
		
	    
		//make sure finalT is larger then T before executing
		if(finalT <= T) System.out.print("Temperature is out of range");
		
		else {
			// calculate temperature increment
			double inc = (finalT - T)/steps;
			// temp for execution
			double runT = T;
			//set up the model
			SetUp(model, N, P);
			
			//execute
			for(int z = 0; z <= steps; z++) {
				//draw the model during the run 
				if(drawModel == true) {
					for(int i = 0; i <= pass; i++) {
						Index (model,  N,  J,  runT);
						if(i % update == 0) DrawModel(model,N,drawModel);	
					}
				}
				//if drawModel is not set to true
				else {
					for(int i = 0; i <= pass; i++) {
						
						Index (model,  N,  J,  runT);
					}
				}
				//print out each step
				M = TotalMagnetization (model,  N,  M);
				HC = HeatCapacity(model, N, J, runT);
				System.out.print(runT);
				System.out.print("\n");
				//write results to array
				resultsArray[resultsIndex][0] = runT;
				resultsArray[resultsIndex][1] = M;
				resultsArray[resultsIndex][2] = HC;
				resultsIndex ++;

				runT += inc;
				//SetUp(model, N, P);
				

				
			}//end steps for
			
		}// end else
		//print out the array
		for(int q = 0; q < resultsIndex; q++) {
			System.out.print(resultsArray[q][0]);
			System.out.print("                                  ");			
			System.out.print(resultsArray[q][1]);
			System.out.print("                                  ");
			System.out.print(resultsArray[q][2]);	
			System.out.print("\n");
		}
		//write the array to a file for use
		try {
			FileWriter myWriter = new FileWriter("ising.txt");
			for	(int q = 0; q < resultsIndex; q++) {
			 	String str = String.valueOf(resultsArray[q][0]);
				myWriter.write(str);
				myWriter.write(";");
			 	str = String.valueOf(resultsArray[q][1]);
				myWriter.write(str);
				myWriter.write(";");
			 	str = String.valueOf(resultsArray[q][2]);
				myWriter.write(str);
				myWriter.write(";");
				myWriter.write("\n");
				
				
			}
			myWriter.close();
		      	
		}
		catch(IOException e) {
			 System.out.println("IOException : " + e);
		}

	}//end RunVarT
	
	
	
	public static void RunSingleT(int model[][], int N, int update, int pass, 
			double M, double HC, double T, double P, double J, boolean drawModel){
		
		/*
		 * INPUTS: 
		 * 	N							 Array Size
		 *	update						 sed to control how often the graphic is updated when update mod pass = 0
		 *	pass						 Number of passes to execute
		 *	M							 Magnetization
		 *	HC							 Heat Capacity Heat Capacity in J/Kb^2
		 *	T							 Temperature in J/KbT
		 *	P							 Probability of spin up between 0 and 1
		 *	J							 Strength of interaction between spins
		 *	model[][] 	 		         Initialize the array	with 2 extra rows and columns for boarders
		 *	drawModel			  		 used to indicate if the model should be drawn during the sim or not.
		 * RETURNS:
		 * 		model      				NxN Array (NOTE: in java arrays are passed by referance so model is not directly
		 * 								returned but the method still updates it)
		 * 
		 * This method runs the model from T to finalT with steps number of steps
		 */
		
		//set up the model
		SetUp(model, N, P);
		
		//draw the model during the run 
		if(drawModel == true) {
			for(int i = 0; i <= pass; i++) {
				Index (model,  N,  J,  T);
				if(i % update == 0) DrawModel(model,N,drawModel);	
			}
		}
		//if drawModel is not set to true
		else {
			for(int i = 0; i <= pass; i++) {
				Index (model,  N,  J,  T);
			}
		}
			
		M = TotalMagnetization (model,  N,  M);
		HC = HeatCapacity(model, N, J, T);
		System.out.print(T);
		System.out.print("                                  ");			
		System.out.print(M);
		System.out.print("                                  ");
		System.out.print(HC);
		

	}
	
	public static void SetUp(int model[][], int N, double P){
		/*Inputs:
		 * 	model = N+2 x N+2 array
		 * 	p = probability of spin
		 *
		 *Returns:
		 *	model with values          NxN Array (NOTE: in java arrays are passed by referance so model is not directly
		 * 								returned but the method still updates it)
		 *
		 *This method assigns a -1,0,1 values to each array element based on a random number
		 *being smaller then the value of p.  0 is reserved for boarder cells so they will 
		 *not interact with the model.  N + 2 is used to give room for boarder cells so we
		 *will not run off the array when calculating neighbors energy.
		 */
		for(int i = 0; i < N+2; i++) {
			for(int j = 0; j < N+2; j++) {
				if		(i == 0 || j == 0 || i == N+1 || j == N+1)		model[i][j] =  0;
				else if	( (Math.random() < P)) 	model[i][j] = -1;
				else							model[i][j] =  1;	//change to -1 to set up a polarized Array
			}
		}
		
	}//end SetUp
	
	public static void DrawModel (int model[][], int N, boolean drawModel){
		/*
		 * This method draws a colored square for each position of the array based on its
		 * value.  
		 * true = Blue
		 * false = Red        
		 */	
		if(drawModel == true) {	
			for(int i = 0; i < N+2; i++) {
				for(int j = 0; j < N+2; j++) {
					if(model[i][j] == -1) {
						g.setColor(Color.BLUE);
						g.fillRect(5 * i, 5*j, 5, 5);
					}
					else if(model[i][j] == 1){
						g.setColor(Color.RED);
						g.fillRect(5 * i, 5*j, 5, 5);
					}
					else {
						g.setColor(Color.WHITE);
						g.fillRect(5 * i, 5*j, 5, 5);
					}
				}
			}
		}
		
	}//end DrawModel
	
	public static double TotalMagnetization (int model[][], int N, double M) {
		/*
		 * Inputs:
		 * 		model = array N+2xN+2
		 * 		N = number of array elements with values of 1 and -1
		 * 	
		 * Returns:
		 * 		total magnetization
		 * 
		 * This method calculates the total magnetization of the model
		 * by summing the states of 1 then dividing by number of states 
		 * squared
		 */
		for(int i = 0; i < N+2; i++) {
			for(int j = 0; j < N+2; j++) {
				M += model[i][j];
			}	
		}
	return Math.abs(M/(N*N));
	}
	
	public static double DeltaEnergy(int model[][], int i, int j, double J, double N ) {
		/*
		 * Input:
		 * 		model	array NxN
		 * 		i, j 	positions in the array
		 * 		J		Strength of interaction between spins
		 * 		N		Size of Array
		 * 
		 * returns:
		 * 		The potential change in energy of of the array position based of nearest neighbors on x and y axis
		 * 
		 * This method calculates the potential change in energy at a place in the array based on the nearest neighbors
		 * on the x and y axis 
		 */
		double deltaE = (-2*model[i][j])*(model[(i+1)][j] + model[(i-1)][j] + model[i][(j+1)] + model[i][(j-1)]);
		
		return deltaE;	
		}

	
	public static void Index (int model[][], int N, double J, double T){
		/*
		 * Inputs:
		 * 		model	NxN array
		 * 		i		position i in array
		 * 		j		positing j in array
		 * 		T		temperature in J/Kb
		 * 		N		size of array
		 * 		J		energy of interaction
		 * 
		 * This method calculates the change in energy of using the DeltaEnergy method
		 * and flips the orientation if the value is less or = 0 indicating it is a 
		 * lower energy state.  the orientation will also flip the exponential of the 
		 * change in energy	divided by the temperature is greater then a random double.
		 * This randomness is the insertion of temperature variation into the model.  
		 * Temperature is given in terms of J/Kb.
		 */
		Random rn = new Random(); // random number gen
		int i = rn.nextInt(N)+1;
		int j = rn.nextInt(N)+1;
		double deltaE = -1*DeltaEnergy(model, i, j, J, N );
		if( deltaE <= 0 || (Math.random() <= Math.exp(-deltaE/T))) model[i][j] *= -1;
		
	}
	
	public static double HeatCapacity(int model[][], int N, double J, double T) {
		double HC = 0.0;  			//Heat Capacity
		double e  = 0;				//energy in a cell
		int E  = 0;					//sum of e
		int E2 = 0;					//sum of e*e
		double U = 0.0;				//energy stored
		double U2 = 0.0;			//area to store energy 
		
		//ignore boundary
		for(int i = 1; i <= N; i++) {
			for(int j = 1; j <= N; j++) {
				e  = DeltaEnergy(model, i, j, J, N);
				E  += e;
				E2 += (e*e);
			}
		}
		U = (1.0/(N*N))*E;
		U2 = (1.0/(N*N))*E2;
		
		HC = ((U2 - (U*U))/(T*T));
		return HC;
	}
	
	
}//end class
