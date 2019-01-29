package project;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.jgap.util.randomHotBits;

public class HillClimbing extends SearchMethod  { //maybe there is this error because there is no a RandomSearch? because Hill Climbing is a son of RandomSearch

	protected double neighborhoodSize;
	protected Problem instance;
	protected int populationSize, numberOfIterations;
	protected boolean printFlag, writeFlag, writeCsv;
	protected Solution[] population;

	@Override
		public void run() {
			search();
		}
	public HillClimbing() {
		instance = new Problem(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfIterations = Main.NUMBER_OF_GENERATIONS;
		neighborhoodSize = Main.NUMBER_OF_NEIGHBORS; //need to insert this parameter in the main class, NUMBER_OF_NEIGHBORS=1000
				printFlag = Main.printFlag;

	}


	public void search() { //search and main
		for (int i=0; i<numberOfIterations; i++) { //hill climbing until terminal condition


			for(int p=0; p<population.length; p++) { //for every individuals in the population

				Solution bestNeighbor = new Solution(instance);
				double fitnessBestNeighbor = 30000; //a big value
				double BestFitness = getBest(population);
				for (int n=0; n<neighborhoodSize; n++) { //for every individual, find the set of neighbors
					
					Solution neighbor = generateNeighbor(p);
					double fitnessNeighbor = neighbor.getFitness(); 



					if(fitnessBestNeighbor >= fitnessNeighbor) { //without controlling the admissibility?
						bestNeighbor = neighbor.copy();
						fitnessBestNeighbor = fitnessNeighbor;
					}
				}
				
				if ( BestFitness => fitnessBestNeighbor) { //how to get the bestFitness of the solution?
					population[p] = bestNeighbor.copy();
					BestFitness = fitnessBestNeighbor;				
				}

				/*If you want to print
			 if (printFlag) {

			}
				 */


			}


		}
	}


	//neighbor is equals to a solution array changed of 100 values, 10 for triangles and randomly
	protected Solution generateNeighbor(Solution sol) {


		Solution temp = sol.copy();
		Random r=new Random();
		int rand;

		for(int triangleIndex=0; triangleIndex<instance.getNumberOfTriangles(); triangleIndex++) {

			int valueIndex = r.nextInt(Solution.VALUES_PER_TRIANGLE)-triangleIndex;

			if (valueIndex+triangleIndex < 4) {
				rand = r.nextInt(256);
				temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + (valueIndex+triangleIndex)] = rand;
			} else {
				if ((valueIndex+triangleIndex) % 2 == 0) {
					rand = r.nextInt(instance.getImageWidth() + 1);
					temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + (valueIndex+triangleIndex)] = rand;
				} else {
					rand = r.nextInt(instance.getImageWidth() + 1);
					temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + (valueIndex+triangleIndex)] = rand;
				}
			}

		}
		return temp;


	}
	public double getBest(Solution[] Solutions){
		double fitness = 3000000;
		for(int i=0; i<populationSize; i++){
			if(Solutions[i].getFitness() < fitness){
				fitness = Solutions[i].getFitness();
			}
		}
		return fitness;
	}
}
