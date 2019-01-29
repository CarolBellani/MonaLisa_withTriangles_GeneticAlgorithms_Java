package project;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.jgap.util.randomHotBits;

public class SimulatedAnnealing extends HillClimbing {


	protected double controlParameter, updateRate;
	protected Problem instance;
	protected int populationSize, numberOfIterations;
	protected boolean printFlag;
	protected double neighborhoodSize;
	protected Solution[] population;
    protected Random r;


	public SimulatedAnnealing() {
		instance = new Problem(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfIterations = Main.NUMBER_OF_GENERATIONS;
		neighborhoodSize = Main.NUMBER_OF_NEIGHBORS; //need to insert this parameter in the main class
		printFlag = Main.printFlag;
		controlParameter = Main.controlParameter; //we need to add this param in the main
		updateRate = Main.updateRate; //we need to add this param in the main
	}


	public void search() { //search and main

		Solution sol = new Solution(instance); 
		sol.initialize();//initialize a new random solution admissible


		double fitn=3000000;
		for (int i=0; i<numberOfIterations; i++) { //sim annealing until terminal condition
			for (int n=0; n<neighborhoodSize; n++) {
				Solution neighbor = HillClimbing.generateNeighbor(neighbor);
				if(acceptNeighbor(neighbor)) {
					sol=neighbor;
					fitn=neighbor.getFitness();
				}
			}

			updateControlParameter();

			/*If you want to print
			 if (printFlag) {

			}
			 */
		}
	}

	protected boolean acceptNeighbor(Solution neighbor) {

		r = new Random();
		double fitnessNeighbor = neighbor.getFitness() ;

		if (fitnessNeighbor <= neighbor.getFitness())   //why the sol of the search it is not here?
			return true;
		else 
			return  Math.exp(-((neighbor.getFitness() - fitnessNeighbor) / controlParameter)) > r.nextDouble();
	}

	protected void updateControlParameter() { //classic
		controlParameter *= updateRate;
	}


}
