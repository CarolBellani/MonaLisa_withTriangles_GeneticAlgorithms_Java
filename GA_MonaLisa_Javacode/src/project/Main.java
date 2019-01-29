package project;

import gd.gui.GeneticDrawingApp;

public class Main {

	public static final int NUMBER_OF_TRIANGLES = 100;

	public static int NUMBER_OF_RUNS = 10; // not final because of test function
	public static final int NUMBER_OF_GENERATIONS = 2000;
	public static final int POPULATION_SIZE = 25;
	//    ***Initialization***
	public static boolean goodInitialize = false; // set to True to start on the best result obtained
	public static int nEvaluates = 25;  // used on fitness XO and mutation, loops N times until it improves
	//It evaluates(slow) N times after each operation. The higher the better fitness and slower
	//    ***Selection***
	// 1 for Tournament, 2 for Roulette, 3 for Ranking   for parent1 and parent2
	// 4 for Annealed, 5 for elitism, 6 for tournamentRF, 7 for rouletteRF, 8 for Stochastic
	public static int selectionTypeP1=1, selectionTypeP2=1;  //Best 1-Tournament
	public static int TOURNAMENT_SIZE =12; //Best - 12      // Only used if selectionType=1
	//    ***CrossOvers***
	public static double CROSSOVER_PROBABILIY = 0.5;//B 0.5    // 
	public static int crossoverType=18; //type of XO Best - 14, using nEvaluates Best - 18
	//1-singlepointXO, 2-twopointXO, 3-avgXO, 4-KpointXO, 5-onebyoneXO
	//6-matingXO(triangle), 7-matingXO(Value), 8-4wayXO, 9- DiscreteXO, 10-flatXO
	//11-orderXO, 12-independentXO 13-cycleXO, 14-relativeFitnessXO 15-relativeFtwopointXO
	//16-cycleXORF 17-twoPointFitnessXO 18-RFonSteroidsXO
	public static int kpointsXO = 10;    // Only in k-points XO
	//   ***Mutations***
	public static double MUTATION_PROBABILIY = 0.9; //B 0.9
	public static int mutationType=7; // Best - 2, using nEvaluates Best - 7 
	// 1 - mutate 1val 2- mutate N values 3- boxMutation 4- Difference mutation 5-AllMutation
	// 6 - trianglePosMutation 7- fitnessMutation 8 - insertMutation 9 - inversionMutation
	// 10 - relativeFitnessMutation 11 - scrambleMutation 12-BoxFitnessMutation 13 - boxFitnessMutationLooped
	public static int mutationNumberPerSol=1;  //Best - 1 // N of mutation values
	                   /*  Parameters for AllMutation, too powerfull. */
					   /*  all values at 1 would be random search and mona lisa's hearth beating */
	public static double mutationProbIndv = 1; // Prob of mutation by indv
	public static double mutationProbTriangle = 1; // Prob of mutation by Triangle of the indv
	public static double mutationProbValue = 1; // Prob of mutation by Value inside each triangle
	public static double mutationProbControled = 0; // prob of doing controled mutation   
	public static int mutationControledPerc = 10; // +- Control the mutation range
	// ***Auxiliary***
	public static boolean activateRestrictedMating=false;
	public static int rateSimiliratyP = 5; // rate similiratiy of parents for restricted mating
	
	public static boolean changeSelectionDuringRun=false, 
				changeXOduringRun=false, changeMutationDuringRun=false; //true for genotypicVar, false for phenotypicVar
	public static boolean activateInversion=false, activateFitnessSharing=false,genVsPhen=false; 
	public static boolean checkColor=false, checkCord=false, checkPerimeter=false;
	public static int NInversions= 100;
	public static int nElites =1; //Best - 1    // Elitism --> N of elites to select

	public static boolean KEEP_WINDOWS_OPEN = false;
	public static boolean WRITE_TXT_FILE = true;

	public static Solution[] bestSolutions = new Solution[NUMBER_OF_RUNS];
	public static double[] bestFitness = new double[NUMBER_OF_RUNS];
	public static int currentRun = 0;
	/*     ******  Write Variables  ******   */
	public static int runtoWrite=100;						//Write every X generations
	public static boolean printFlag=false, writeCsv=true;	// Print to file
	public static String FileNameWrite = "goodinitalizeLE.csv";    //File name to write, must end on  ".txt"
	public static String notas = "";  // Notes to print about run
	// ******Hill Climbing && Simulated Annealing *****
	public static int NUMBER_OF_NEIGHBORS = 20;
	public static int controlParameter;
	public static int updateRate;
	
	
	public static void main(String[] args) {
		run();
	}
	
	public static void addBestSolution(Solution bestSolution) {
		bestSolutions[currentRun] = bestSolution;
		bestFitness[currentRun] = bestSolution.getFitness();
		System.out.printf("Got %.2f as a result for run %d\n", bestFitness[currentRun], currentRun + 1);
		System.out.print("All runs:");
		for (int i = 0; i <= currentRun; i++) {
			System.out.printf("\t%.2f", bestFitness[i]);
		}
		System.out.println();
		currentRun++;
		if (KEEP_WINDOWS_OPEN == false) {
			Problem.view.getFittestDrawingView().dispose();
			Problem.view.getFrame().dispose();
		}
		if (currentRun < NUMBER_OF_RUNS) {
			run();
		} else {
			presentResults();
		}
	}

	public static void presentResults() {
		double mean = Statistics.mean(bestFitness);
		double stdDev = Statistics.standardDeviation(bestFitness);
		double best = Statistics.min(bestFitness);
		double worst = Statistics.max(bestFitness);
		System.out.printf("\n\t\tMean +- std dev\t\tBest\t\tWorst\n\n");
		System.out.printf("Results\t\t%.2f +- %.2f\t%.2f\t%.2f\n", mean, stdDev, best, worst);
	}

	public static void run() {
		GeneticDrawingApp.main(null);
	}
	public static void runHillClimning() {
		HillClimbing.main(null);
	}
}
