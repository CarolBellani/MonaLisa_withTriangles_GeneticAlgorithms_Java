package project;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Random;
import java.util.Scanner;

import javax.swing.text.MutableAttributeSet;

import org.jgap.gp.function.Min;

public class GeneticAlgorithm extends SearchMethod {
	protected Problem instance;
	protected Solution[] population;
	protected Solution currentBest;
	protected int populationSize, numberOfGenerations, currentGeneration, tournamentSize, mutation_By,
				mutationControledPerc,nElites,mutationNumberPerSol, kpointsXO, selectionTypeP1, 
				selectionTypeP2, crossoverType,mutationType, rateSimiliratyP, nEvaluates, countMutationI;
	protected double gen0,mutationProbability, crossoverProbability, mutationProbIndv, 
			mutationProbTriangle, mutationProbValue,mutationProbControled;
	protected boolean printFlag, writeFlag, writeCsv;
	protected Random r;
	protected String FileNameWrite;
	protected int runtoWrite;
	protected String notas;
	protected boolean applyFavSon=false, activateInversion,
			 activateFitnessSharing,genVsPhen, goodInitialize;
	protected boolean activateRestrictedMating, changeXOduringRun, changeSelectionDuringRun
					, changeMutationDuringRun;
	protected boolean checkColor, checkPerimeter, checkCord;
	protected int degreeRestrictedMating = 100;
				//Number of inversions and reorders to do
	protected int NInversions;    // Needs to be here for the matrix to populate correctly

	protected int[][] indexesInv; // Indexes of Initial and final pos
	protected int[][] indexesPop; 
	int count=0;
	
	public GeneticAlgorithm() {
		instance = new Problem(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
		mutationProbIndv = Main.mutationProbIndv;
		mutationProbTriangle = Main.mutationProbTriangle;
		mutationProbValue = Main.mutationProbValue;
		mutationProbControled = Main.mutationProbControled;
		mutationControledPerc = Main.mutationControledPerc;
		mutationNumberPerSol = Main.mutationNumberPerSol;
		crossoverProbability = Main.CROSSOVER_PROBABILIY;
		kpointsXO = Main.kpointsXO;
		activateFitnessSharing = Main.activateFitnessSharing;
		genVsPhen = Main.genVsPhen;
		tournamentSize = Main.TOURNAMENT_SIZE;
		nElites = Main.nElites;
		activateInversion = Main.activateInversion;
		activateRestrictedMating = Main.activateRestrictedMating;
		NInversions = Main.NInversions;
		rateSimiliratyP = Main.rateSimiliratyP;
		selectionTypeP1 = Main.selectionTypeP1;
		selectionTypeP2 = Main.selectionTypeP2;
		crossoverType = Main.crossoverType;
		mutationType = Main.mutationType;
		printFlag = Main.printFlag;
		changeSelectionDuringRun = Main.changeSelectionDuringRun;
		changeXOduringRun = Main.changeXOduringRun;
		changeMutationDuringRun = Main.changeMutationDuringRun;
		currentGeneration = 0;
		goodInitialize = Main.goodInitialize;
		nEvaluates = Main.nEvaluates;
		checkColor = Main.checkColor;
		checkCord = Main.checkCord;
		checkPerimeter = Main.checkPerimeter;
		r = new Random();
		writeFlag = Main.WRITE_TXT_FILE;
		writeCsv = Main.writeCsv;
		runtoWrite = Main.runtoWrite;  // write gen info every runtoWrite generations
		FileNameWrite = Main.FileNameWrite;
		
		notas = Main.notas;
	}

	public void run() {
		initialize();
		search();
		Main.addBestSolution(currentBest);
	}

	public void initialize() {
		population = new Solution[populationSize];
		for (int i = 0; i < population.length; i++) {
			population[i] = new Solution(instance);
			population[i].evaluate();
		}
		
		updateCurrentBest();
		updateInfo();
		currentGeneration++;
		if(goodInitialize)
			readValues();
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}

	public void search() {
		gen0 = getBest(population).getFitness();
		if(goodInitialize)
			readValues();
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offsprings = new Solution[populationSize]; // P'
			Solution favoriteSon = population[0];
			for (int k = 0; k < population.length; k++) {
				
				if(activateFitnessSharing) {
					fitnessSharing(k);  // Not implemented at 100%, only works for tournament
					}
				
				// configure parameters to change during run
				if(changeSelectionDuringRun && currentGeneration>1000) {
					selectionTypeP1= 8;
					selectionTypeP2= 8;
				}
				
				int p1=26, p2=26;
				
				switch(selectionTypeP1) {
					case 1: p1 = tournamentSelection();
							break;
					case 2: p1 = rouletteWheelSelection();
							tournamentSize=0;  // Not used, easier to analize stats 
							break;
					case 3: p1 = rankingSelection();
							tournamentSize=0;  // Not used, easier to analize stats
							break;
					case 4: p1 = annealedSelection();
							break;
					case 5: p1 = elitismSelection(k);
							break;
					case 6: p1 = RelativeFtournamentSelection();
							break;
					case 7: p1 = rouletteWheelSelectionRF();
							break;
					case 8: p1 = StochasticSelection(6); // n of pointers
							break;

					default: System.out.println("Invalid selection Type");
				}
				switch(selectionTypeP2) {
					case 1: p2 = tournamentSelection();
							break;
					case 2: p2 = rouletteWheelSelection();
							tournamentSize=0;  // Not used, easier to analize stats
							break;
					case 3: p2 = rankingSelection();
							tournamentSize=0;  // Not used, easier to analize stats	
							break;
					case 4: p2 = annealedSelection();
							break;
					case 5: p2 = elitismSelection(k);
							break;
					case 6: p2 = RelativeFtournamentSelection();
							break;
					case 7: p2 = rouletteWheelSelectionRF();
							break;
					case 8: p2 = StochasticSelection(6);
							break;
					default: System.out.println("Invalid selection Type");
			}
				
				int[] parents = {p1, p2};
				
				if(activateRestrictedMating) {
					while(rateSimilarity(parents) < rateSimiliratyP && count<100) {
						tournamentSize=4;
						p2 = tournamentSelection();
						count++;
					}
				 tournamentSize=10;  count=0;
				 }
				
				if (r.nextDouble() <= crossoverProbability) {
					
					// configure parameters to change during run
					if(changeXOduringRun && currentGeneration>1000 && currentGeneration%2==0) {
						//crossoverType = 13;
						crossoverProbability = 0;
					}
					/*
					if(k==0)
						crossoverType = 13;
					if(k==12)
						crossoverType = 14;
						*/
					if(activateInversion) {
				
						twoInversion(parents);
				
						switch(crossoverType) {
							case 1: offsprings[k] = singlePointCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 2: offsprings[k] = twoPointCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 3: offsprings[k] = avgCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 4: offsprings[k] = kPointCrossover(parents, kpointsXO);
									break;
							case 5: offsprings[k] = onebyoneCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 6: offsprings[k] = matingCrossover(parents); 
									kpointsXO=0;
									break;
							case 7: offsprings[k] = matingCrossoverValue(parents); 
									kpointsXO=0;
									break;
							case 8: int parent[] = {p1, p2,tournamentSelection(), tournamentSelection()};  
									offsprings[k] = fourWayCrossover(parent); 
									kpointsXO=0;
									break;
							case 9: offsprings[k] = discreteCrossover(parents); 
									kpointsXO=0;
									break;
							case 10:offsprings[k] = flatCrossover(parents, true);  
									kpointsXO=0;	//set true for Max, false for Min value
									break;
							case 11:offsprings[k] = orderCrossover(parents);
									break;
							case 12:offsprings[k] = independentCrossover(parents);
									break;
							case 13:offsprings[k] = cycleCrossover(parents);
									break;							
							case 14: offsprings[k] = relativeFitnessCrossover(parents);
									break;
							case 15: offsprings[k] = relativeF10PointCrossover(parents); // must receive 10 parents
									break;
							case 16: offsprings[k] = cycleCrossoverRF(parents);
									break;
							case 17: offsprings[k] = twoPointFitnessCrossover(parents, nEvaluates);
									break;
							case 18: offsprings[k] = RFonSteroidsCrossover(parents, nEvaluates);
									break;
							case 19: offsprings[k] = singlePointCrossoverFitness(parents);
									break;
							default: System.out.println("Invalid selection Type");
									break;
						}
				
						reorderALL(population);

					}
						
					else {
						NInversions = 0;  // Not used, easier to analize stats	
						switch(crossoverType) {
							case 1: offsprings[k] = singlePointCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 2: offsprings[k] = twoPointCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 3: offsprings[k] = avgCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 4: offsprings[k] = kPointCrossover(parents, kpointsXO);
									break;
							case 5: offsprings[k] = onebyoneCrossover(parents);
									kpointsXO=0; // Not used, easier to analize stats	
									break;
							case 6: offsprings[k] = matingCrossover(parents); 
									kpointsXO=0;
									break;
							case 7: offsprings[k] = matingCrossoverValue(parents); 
									kpointsXO=0;
									break;
							case 8: int parent[] = {p1, p2,tournamentSelection(), tournamentSelection()};  
									offsprings[k] = fourWayCrossover(parent); 
									kpointsXO=0;
									break;
							case 9: offsprings[k] = discreteCrossover(parents); 
									kpointsXO=0;
									break;
							case 10:offsprings[k] = flatCrossover(parents, false);  
									kpointsXO=0;	//set true for Max, false for Min value
									break;
							case 11:offsprings[k] = orderCrossover(parents);
									break;
							case 12:offsprings[k] = independentCrossover(parents);
									break;
							case 13:offsprings[k] = cycleCrossover(parents);
									break;
							case 14: offsprings[k] = relativeFitnessCrossover(parents);
									break;
							case 15: int parentt[] = new int[10];
									for(int a=0; a<10; a++) {
										parentt[a] = tournamentSelection();
									}
									offsprings[k] = relativeF10PointCrossover(parentt);
									break;
							case 16: offsprings[k] = cycleCrossoverRF(parents);
									break;
							case 17: offsprings[k] = twoPointFitnessCrossover(parents, nEvaluates);
									break;
							case 18: offsprings[k] = RFonSteroidsCrossover(parents, nEvaluates);
									break;
							case 19: offsprings[k] = singlePointCrossoverFitness(parents);
									break;
							default: System.out.println("Invalid selection Type");
									break;
						}
					} 
					
				}
				else
					offsprings[k] = population[p1];
				// configure parameters to change during run
				if(changeMutationDuringRun  && currentGeneration>1000) {
					//mutationType = 2;
					//activateFitnessSharing = true;
					mutationType = 14;
					crossoverProbability = 0;
					mutationNumberPerSol = 1;
					nEvaluates = 100;
				}
				if (r.nextDouble() <= mutationProbability) {
					switch(mutationType) {
						case 1: offsprings[k] = offsprings[k].applyMutation();
								break;
						case 2: offsprings[k] = offsprings[k].applyMutationN(mutationNumberPerSol);
								break;
						case 3: offsprings[k] = offsprings[k].boxMutation(100, 100);
								break;
						case 4: offsprings[k] = differenceMutation(parents, 100);
								break;
						case 5: offsprings[k] = AllMutation(offsprings[k]);
								break;
						case 6: offsprings[k] = trianglePosMutation(offsprings[k], r.nextInt(100));
								break;

						case 7 :offsprings[k] = fitnessMutation(offsprings[k], mutationNumberPerSol, nEvaluates);
								break;    // May not be allowed, IS SLOWWWW
						case 8:offsprings[k] = insertMutation(offsprings[k], mutationNumberPerSol);
								break;
						case 9:offsprings[k] = inversionMutation(offsprings[k], mutationNumberPerSol);
								break;
						case 10:offsprings[k] = applyMutationNrelativeF(offsprings[k], mutationNumberPerSol);
								break;
						case 11:offsprings[k] = scrambleMutation(offsprings[k], mutationNumberPerSol, r.nextBoolean(), 10);
								break;
						case 12:offsprings[k] = boxFitnessMutation(offsprings[k], mutationNumberPerSol, nEvaluates,10);
								break;
						case 13:offsprings[k] = boxFitnessMutationLooped(offsprings[k], mutationNumberPerSol, nEvaluates, 10);
								break;
						default: System.out.println("Invalid mutation option");
								break;
					}
				} 
				/*      ***************** TEST  ***********************  */
				//System.out.println("fit "+population[k].getFitness()+"  shared "+population[k].getsharedFitness());
				/*      ***************** TEST  ***********************  */
				if(checkColor || checkPerimeter) {
					for(int i=0; i<100; i++) {
						if(checkColor)
							offsprings[k] = checkColor(offsprings[k], i);
						if(checkPerimeter)
							offsprings[k] = checkPerimeter(offsprings[k], i);
					} 
				}
				if(checkCord)
					offsprings[k]= checkCord(offsprings[k]);
				
				offsprings[k].evaluate();
				if(offsprings[k].getFitness() < population[parents[0]].getFitness() && 
						offsprings[k].getFitness() < population[parents[1]].getFitness()) {
					offsprings[k].decreaseRelativeFitness();
				} else
					offsprings[k].increaseRelativeFitness();
			//population = replacement(offsprings);
			} 
			
			population = Elitism(offsprings, nElites);
			updateCurrentBest();
			updateInfo();
			currentGeneration++;
			//printExcTeste();
			//if(writeFlag && currentGeneration%runtoWrite==0)
				//printTxt();
			if(writeCsv && currentGeneration%runtoWrite==0 ) {
				//printExcTeste();
				printExcTesteCOUNTS();
				Stats_Report();
			}
		}
	}
	//                                      ************************************************
	//                                      --------------- Selection ----------------------
	//                                      ************************************************
	// --- Tournament Selection
	protected int tournamentSelection() {
		int parentIndex = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if(activateFitnessSharing) {
				if (population[temp].getsharedFitness() < population[parentIndex].getsharedFitness()) {
					parentIndex = temp;
				}
			} else {
				if (population[temp].getFitness() < population[parentIndex].getFitness()) {
					parentIndex = temp;
				}
			}
		}
		return parentIndex;
	} 
	
	protected int RelativeFtournamentSelection() {
		int parentIndex = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getAvgRelativeFitness() < population[parentIndex].getAvgRelativeFitness()) {
					parentIndex = temp;				// is higher cause Rfitness if higher is better
			}
		}
		return parentIndex;
	}
	
	//--- Roulette wheel
	protected int rouletteWheelSelectionRF() {
		double rand = r.nextDouble(), acumulatedFitness = 0;
		int i;
		for(i=0; i<populationSize; i++) {
			acumulatedFitness += population[i].getAvgRelativeFitness()/totalRFitness();
			if (acumulatedFitness >= rand) 
				break;
		}
		return i;
	}
	protected int rouletteWheelSelection() {
		int rand=0, acumulatedFitness = 0;
		int j;
		for(int i=0; i<populationSize; i++) {
			if(activateFitnessSharing)
				acumulatedFitness += population[getWorstIndex(population)].getsharedFitness() - population[i].getsharedFitness();
			else
				acumulatedFitness += population[getWorstIndex(population)].getFitness() - population[i].getFitness();
			

		}
		rand = r.nextInt(acumulatedFitness);
		for(j=0; j<populationSize; j++) {
			if (acumulatedFitness >= rand) 
				break;
		}
		return j;
	} //we changed the Wheel selection copy and paste from practical classes
	
	protected int StochasticSelection(int pointers) {
		double[] fitnesses = new double[25]; 
		int i=0; int[] result = new int[pointers];
		double[] indv = new double[pointers];
		int[] acumulatedFitness = new int[25];

		for(i=0; i<populationSize; i++) {
			fitnesses[i] = population[getWorstIndex(population)].getFitness() - population[i].getFitness();
		}  Arrays.sort(fitnesses); //low to high
		for(int j=1; j<=pointers;j++) {
			indv[j-1] = (1/(double)pointers) * (double) j *  fitnesses[24]; 
		}
		double closer = 10000;
		for(int b=0; b<pointers; b++) {
			for(int a=0; a<populationSize; a++) {
				if(Math.abs( fitnesses[a] - indv[b])  < closer) {
					//System.out.println(Math.abs( fitnesses[a] - indv[b])+ "   closer  "+closer);
					closer = Math.abs(fitnesses[a] - indv[b]);
					result[b] = a;
				}
			}
		}
		
		return result[r.nextInt(pointers)];
	}
	
	int[] fitnesssortasc=new int[populationSize];
	int[] fitness1=new int[populationSize];
	int[] arrayindexpop= new int[populationSize];
	//---Ranking selection	//---Ranking selection
	protected int rankingSelection() {
		double probability=0, rand = r.nextDouble() /*,logprob*/ ;
		double sum=0;
		int[] fitnesssortasc=new int[populationSize];
		int[] fitness1=new int[populationSize];
		int[] arrayindexpop= new int[populationSize];
		int j;
		int selected=0;
		if(activateFitnessSharing) {
			for (int i=1; i<=fitness1.length; i++) {
				fitness1[i-1] = (int) population[i-1].getsharedFitness(); //is not yet sorted, is just populated 
				sum += i;
			}
		} else {
			for (int i=1; i<=fitness1.length; i++) {
				fitness1[i-1] = (int) population[i-1].getFitness(); //is not yet sorted, is just populated 
				sum += i;
			}
		}
			
		fitnesssortasc= fitness1.clone();
		Arrays.sort(fitnesssortasc);
		for(int i =0; i<arrayindexpop.length; i++) {
			for(int a=0; a<arrayindexpop.length;a++) {
				if(fitnesssortasc[i]==fitness1[a]) {
					arrayindexpop[i]=a;
					fitness1[a] = 9999;
					break;
				}
			}
		}
		for (j=1; j<=arrayindexpop.length-1; j++) {
			probability += Math.log((arrayindexpop[arrayindexpop.length-j] / sum)); // linear

			if (probability >= rand) {
				selected = arrayindexpop[j];
				break;
			}
		}
		return selected;
	}
	// Found from a paper, changes selection pressure during run 
	protected int annealedSelection() {
		double[] fitnessCompt = new double[populationSize];
		double S=0, totalComptF=0; int selected=0;
		if(activateFitnessSharing) {
			for(int i=0; i<populationSize; i++) {
				fitnessCompt[i] = population[i].getsharedFitness() / ((2000+1)-currentGeneration);
			} 
		} else {
			for(int i=0; i<populationSize; i++) {
				fitnessCompt[i] = population[i].getFitness() / ((2000+1)-currentGeneration);
			}
		}
		for(int i=0; i<populationSize; i++) {
			S+= fitnessCompt[i];
		} int rand = r.nextInt((int)S);
		double cumFitness=0;
		for(int i=0; i<populationSize; i++) {
			cumFitness+=fitnessCompt[i];
			if(rand<=cumFitness) {
				selected=i;
				break;
			}
		}
		return selected;
	}
	// Selection that just takes the best Indv
	protected int elitismSelection(int k) {
		sortSolutions(population);
		if(k==25)
			k=23;
		return k;
	}
	
	//                                      ************************************************
	//                                      --------------- Crossover ----------------------
	//                                      ************************************************
	
	// --------------- Variation
	// --- Single Point Crossover 
	public Solution singlePointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}
	
	// Is like partially matched XO, takes a "windows" from one of the parents
	public Solution twoPointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		if(crossoverPoint1> crossoverPoint2) {
			int temp = crossoverPoint2;
			crossoverPoint2 = crossoverPoint1;
			crossoverPoint1 = temp;
		}
		for (int i = crossoverPoint1; i < crossoverPoint2; i++) {
			offspring.setValue(i, secondParent.getValue(i));
			offspring.setRelativeFitness(i, secondParent.getRelativeFitness(i));
		}
		
		return offspring;
	}
	// Takes avg value of both parents
	public Solution avgCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy(); int val=0;
		for(int i=0; i<instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			val=(firstParent.getValue(i)+secondParent.getValue(i))/2;
			offspring.setValue(i, val);;
		}
		return offspring;
	}
	
	public Solution kPointCrossover(int[] parents, int kpoints) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int[] krandoms = new int[kpoints];
		
		for(int i=0; i<kpoints; i++) {
			krandoms[i] = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		} Arrays.sort(krandoms);
		
		for(int i=kpoints-1; i>0; i-=2) {
			int crossoverPoint1 = krandoms[i];
			int crossoverPoint2 = krandoms[i-1];
			
			for (int j = crossoverPoint1; j < crossoverPoint2; j++) {
				offspring.setValue(j, secondParent.getValue(j));
				offspring.setRelativeFitness(j, secondParent.getRelativeFitness(j));
			}
		}
		return offspring;
	}
	// One value from each parent
	public Solution onebyoneCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(i%2==0)
				offspring.setValue(i, firstParent.getValue(i));
			else
				offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}

	//only does crossover on triangles that have a different rate of similarity
	// Needs adjustements
	public Solution matingCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(rateSimilarityTriangle(parents, i/10) > 	2) {
				count++;
				offspring.setValue(i, firstParent.getValue(i));
			}
			else
				offspring.setValue(i, secondParent.getValue(i));
		} 
		return offspring;
	}
	//Same as before but it does XO by value, not by Triangle
	public Solution matingCrossoverValue(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(rateSimilarityValue(parents, i) > 2) {
				offspring.setValue(i, firstParent.getValue(i));
				count++;
			}
			else
				offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}
	// XO with 4 parents, was running out of imagination
	public Solution fourWayCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution thirdParent = population[parents[2]];
		Solution forthParent = population[parents[3]];
		Solution offspring = firstParent.copy();
		int crossoverpoints[] = new int[4];
		for(int i=0; i<4; i++) {
			crossoverpoints[i] = r.nextInt(1000);
		}
		Arrays.sort(crossoverpoints);
		for(int j=0; j<3; j++) {
			for (int i = crossoverpoints[3-j]; i < crossoverpoints[2-j]; i++) {
				if(j==0)
					offspring.setValue(i, secondParent.getValue(i));
				else if(j==1)
					offspring.setValue(i, thirdParent.getValue(i));
				else
					offspring.setValue(i, forthParent.getValue(i));
			}
		}
		return offspring;
		
	}
	// flips a coin if it chooses the value from one parent or the other
	public Solution discreteCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(r.nextBoolean()) //Coinflip
				offspring.setValue(i, secondParent.getValue(i));
			else
				offspring.setValue(i, firstParent.getValue(i));
		}
		return offspring;
	}
	// set true for Max, false for min
	// Takes the biggest (if true) or smallest value(if false) of the parents
	public Solution flatCrossover(int[] parents, boolean max) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(max) {
				if(secondParent.getValue(i) > firstParent.getValue(i))
					offspring.setValue(i, secondParent.getValue(i));
				else
					offspring.setValue(i, firstParent.getValue(i));
					}
			else {
				if(secondParent.getValue(i) < firstParent.getValue(i))
					offspring.setValue(i, secondParent.getValue(i));
				else
					offspring.setValue(i, firstParent.getValue(i));
					}
			}
		return offspring;
	}
	// hard to explain, was on a paper
	public Solution orderCrossoverValue(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int[] markedValues = new int[1000];
		int crossoverPoint1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		if(crossoverPoint1> crossoverPoint2) {
			int temp = crossoverPoint2;
			crossoverPoint2 = crossoverPoint1;
			crossoverPoint1 = temp;
		}
		for (int i = crossoverPoint1; i < crossoverPoint2; i++) {
			offspring.setValue(i, secondParent.getValue(i));
			markedValues[i] = secondParent.getValue(i);
		}
		for(int i=crossoverPoint2; i< instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(Arrays.asList(markedValues).contains(firstParent.getValue(i))) {
				for (int j = crossoverPoint1; j < crossoverPoint2; j++) {
					if(Arrays.asList(markedValues).contains(secondParent.getValue(j)))
						continue;
					else {
						offspring.setValue(i, secondParent.getValue(j));
						break;  }
				}
			}
		}
		for(int i=0; i<crossoverPoint1; i++) {
			if(Arrays.asList(markedValues).contains(firstParent.getValue(i))) {
				for (int j = crossoverPoint1; j < crossoverPoint2; j++) {
					if(Arrays.asList(markedValues).contains(secondParent.getValue(j)))
						continue;
					else {
						offspring.setValue(i, secondParent.getValue(j));
						break; }
				}
			}
		}
		return offspring;
	}
	public Solution orderCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		boolean[] markedValues = new boolean[1000];
		int crossoverPoint1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		if(crossoverPoint1> crossoverPoint2) {
			int temp = crossoverPoint2;
			crossoverPoint2 = crossoverPoint1;
			crossoverPoint1 = temp;
		}
		for(int i=crossoverPoint1; i<crossoverPoint2; i++) {
			markedValues[offspring.getIndex(i)] = true;
		}
		int index=0;
		boolean flag=true;
		while(flag) {
			flag=false;
			if(crossoverPoint1<= index && index <= crossoverPoint2) {
				index++; continue;
			}
			if(markedValues[secondParent.getIndex(index)] == false) {
				offspring.setValueIndex(index, secondParent.getValue(index),
						secondParent.getIndex(index));
			} /// no need for else cause offspring already as p1 values  
			index++;
			for(int i=0; i<1000; i++) {
				if(markedValues[i] == false)
					flag=true;
			}
		}
		return offspring;
	}
	// crossover by color or by triangle, doesnt mix then
	public Solution independentCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int rColorpoint = r.nextInt(4); int rIndexpoint = r.nextInt(6)+4;
		
		for(int i=0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			if(i%10 <4) {
				if(rColorpoint < i)
					offspring.setValue(i, secondParent.getValue(i));
			} 
			else {
				if(rIndexpoint < i) {
					offspring.setValue(i, secondParent.getValue(i));
					}
			}  
			if(i%10==9) {
				rColorpoint = r.nextInt(4); 
				rIndexpoint = r.nextInt(6)+4;
			}
		}
		return offspring;
	}
	//cycle XO from class
	public Solution cycleCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		boolean[] marked = new boolean[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];
		int pos = 0,element=0, j=0, cicles=0, index=0;
		boolean flag=true;
		while(flag) {
			while(marked[pos]==false) {
				marked[pos] = true;
				element = secondParent.getValue(pos);
				index = secondParent.getIndex(pos);
				
				offspring.setValueIndex(pos, element, index);
				
				//System.out.println("pos "+pos+"  element "+element);
				for(j=0; j<instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
					if(firstParent.getIndex(j)==index && marked[j]==false) {
						pos=j;
						j=0;
						//System.out.println("pos "+pos);
						break;
					}
				} 
				
			} cicles++;
			for(int i=0; i<marked.length; i++) {
				if(marked[i]==false) {
					pos=i;
					break;
				}
				else if(marked[999]){
					flag=false;
				}
			}
			if(cicles>100)
				flag=false;
		} 
		return offspring;
	}
	//same as before but adjusted to use relative Fitness
	//Needs adjustments. How can we use relative Fitness to increase the odds of a good XO?
	public Solution cycleCrossoverRF(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		boolean[] marked = new boolean[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];
		int pos = 0,element=0, j=0, cicles=0, index=0;
		boolean flag=true;
		while(flag) {
			while(marked[pos]==false) {
				marked[pos] = true;
				element = secondParent.getValue(pos);
				index = secondParent.getIndex(pos);
				
				offspring.setValueIndex(pos, element, index);
				offspring.setRelativeFitness(pos, secondParent.getRelativeFitness(pos));				
				//System.out.println("pos "+pos+"  element "+element);
				for(j=0; j<instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
					if(firstParent.getIndex(j)==index && marked[j]==false) {
						pos=j;
						j=0;
						//System.out.println("pos "+pos);
						break;
					}
				} 
			} cicles++;
			for(int i=0; i<marked.length; i++) {
				if(marked[i]==false) {
					pos=i;
					break;
				}
				else if(marked[999]){
					flag=false;
				}
			}
			if(cicles>100)
				flag=false;
		} 
		return offspring;
	}
	// Uses the relative Fitness of each value to increase the odds of the "better value" to remain
	public Solution relativeFitnessCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int odd=0;
		for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			odd = 0;
			double fpRF = firstParent.getRelativeFitness(i);
			double spRF = secondParent.getRelativeFitness(i);
			//System.out.println("first "+fpRF+"  second  "+spRF);
			odd = (int) (fpRF + spRF);
			//System.out.println("odd "+odd);
			if(r.nextInt(odd) < fpRF) {
				offspring.setValue(i, secondParent.getValue(i));
				offspring.setRelativeFitness(i, secondParent.getRelativeFitness(i));
			}
			else {
				offspring.setValue(i, firstParent.getValue(i));
				offspring.setRelativeFitness(i, firstParent.getRelativeFitness(i));
			}
		}
		return offspring;
	}
	//Experiment with Relative fitness and 10 parents (used with selecting the 10 best parents at each 100 values of the solution
	public Solution relativeF10PointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int index=0;
		for(int i=0; i<1000; i++) {
			index=i/100;
			//System.out.println("index "+index+" i val "+i);
			offspring.setValue(i, population[parents[index]].getValue(i));
			offspring.setRelativeFitness(i, population[parents[index]].getRelativeFitness(i));
		}
		
		return offspring;
	}
	//needs modifications, not sure if allowed when complete
	public Solution twoPointFitnessCrossover(int[] parents, int nTimes) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		Solution temp = offspring.copy();
		for(int j=0; j<nTimes; j++) {
			temp = offspring.copy();
			int crossoverPoint1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
			int crossoverPoint2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
			if(crossoverPoint1> crossoverPoint2) {
				int tempa = crossoverPoint2;
				crossoverPoint2 = crossoverPoint1;
				crossoverPoint1 = tempa;
			}
			for (int i = crossoverPoint1; i < crossoverPoint2; i++) {
				temp.setValue(i, secondParent.getValue(i));
			}
			temp.evaluate();
			if(temp.getFitness() < firstParent.getFitness() && temp.getFitness() < secondParent.getFitness() )
				return temp;
		}
		return temp;
	}
	public Solution RFonSteroidsCrossover(int[] parents, int nTimes) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		Solution temp = offspring.copy();
		int odd=0;
		for(int j=0; j<nTimes; j++) {
			temp = offspring.copy();
			for (int i = 0; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
				odd = 0;
				double fpRF = firstParent.getRelativeFitness(i);
				double spRF = secondParent.getRelativeFitness(i);
				odd = (int) (fpRF + spRF);
				//System.out.println("odd "+odd);
				if(r.nextInt(odd) < fpRF) {
					temp.setValue(i, secondParent.getValue(i));
					temp.setRelativeFitness(i, secondParent.getRelativeFitness(i));
				}
				else {
					temp.setValue(i, firstParent.getValue(i));
					temp.setRelativeFitness(i, firstParent.getRelativeFitness(i));
				}
			}
			temp.evaluate();
			boolean flagi = true;
			if(temp.getFitness() < firstParent.getFitness()) {
				mapValueRF(firstParent, temp, false);
				} else {
					mapValueRF(firstParent, temp, true);
					flagi = false;		
				}
			if(temp.getFitness() < secondParent.getFitness()) {
				mapValueRF(secondParent, temp, false);
			} else {
				mapValueRF(firstParent, temp, true);
				flagi = false;
			}
			if(flagi)
				return temp;
		}
		return temp;
	}
	
	public Solution singlePointCrossoverFitness(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy(); 
		Solution temp = offspring.copy();
		for(int j=0; j<nEvaluates; j++) {
			offspring= temp.copy();
			int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
			for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
				offspring.setValue(i, secondParent.getValue(i));
			}
			temp.evaluate();
			if(temp.getFitness() < firstParent.getFitness() && temp.getFitness() < secondParent.getFitness()) {
				return temp;
			}
		}
		return offspring;
	}

	//                                      ************************************************
	//                                      --------------- Mutation  ----------------------
	//                                      ************************************************
	
	// mutate values that are more differente than range
	public Solution differenceMutation(int [] parents, int range) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		
		boolean[] changes = new boolean[instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE];
		for(int i=0; i<changes.length; i++) {
			if(Math.abs(firstParent.getValue(i)-secondParent.getValue(i)) > range) {
				changes[i] = true;
			}
			else {
				changes[i] = false;
			}
		}
		for(int i=0; i<changes.length; i++) {
			if(changes[i]) {
				int valueIndex= i%10;
				int triangleIndex = i/10;
				if (valueIndex < 4) {
					offspring.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
				} else {
					if (valueIndex % 2 == 0) {
						offspring.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
					} else {
						offspring.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = r
								.nextInt(instance.getImageHeight() + 1);
					}
				}
				
			}
		}
		return offspring;
	}
	// Decide mutation per Indv, Triangle and Value of the Triangle
	// Too powerfull, with parameters at max is like random search
	public Solution AllMutation(Solution sol) {
		if(mutationProbIndv>r.nextDouble()) {
			for(int i=0; i<instance.getNumberOfTriangles(); i++) {
				if(mutationProbTriangle>r.nextDouble()) {
				for(int j=0; j<Solution.VALUES_PER_TRIANGLE;j++) {
					if(mutationProbValue>r.nextDouble()) {
						if ( j < 4) {
							sol.setValue(i*Solution.VALUES_PER_TRIANGLE + j, r.nextInt(256)); 
						} else {
							if(mutationProbControled<r.nextDouble()) {
							if (j % 2 == 0) {
								sol.setValue(i*Solution.VALUES_PER_TRIANGLE+j, 
										r.nextInt(instance.getImageWidth() + 1));
							} else {
								sol.setValue(i*Solution.VALUES_PER_TRIANGLE+j, 
										r.nextInt(instance.getImageHeight() + 1)); 
								}
							} else {
								if (j % 2 == 0) {
									sol.setValue(i*Solution.VALUES_PER_TRIANGLE+j, 
											(r.nextInt((instance.getImageWidth() + 1)-
													(mutationControledPerc*2)) +mutationControledPerc)  );
								} else {
									sol.setValue(i*Solution.VALUES_PER_TRIANGLE+j, 
											r.nextInt((instance.getImageHeight() + 1)-
													(mutationControledPerc*2)) +mutationControledPerc) ; 
									}
								}
							}
						}
					}
				}
			}
		}  return sol;
	} 
	//mutate only if fitness increases, not sure if "legal"
	//reallyyyyy SLOWWWWW
	public Solution fitnessMutation(Solution sol, int nValues, int nTimes) {
				
		Solution temp = sol.copy(); 
		int index;
		double fitness = sol.getFitness(); int count=0;
		
		for(int i=0; i<nTimes; i++) {
			temp = sol.copy();
			for(int j=0; j<nValues; j++) {
				int rand;
				index = r.nextInt(instance.getNumberOfTriangles()* Solution.VALUES_PER_TRIANGLE);
				if (index%10 < 4) {
					rand = r.nextInt(256);
					temp.values[index] = rand;
				} else {
					if (index % 2 == 0) {
						rand = r.nextInt(instance.getImageWidth() + 1);
						temp.values[index] = rand;
					} else {
						rand = r.nextInt(instance.getImageHeight() + 1);
						temp.values[index] = rand;
					}
				}
				
				int tempa = lookupValueRFitness(index, rand);
				temp.setRelativeFitness(index, tempa);
			count++;
		} 
			if(count==nTimes)
				return temp;
		temp.evaluate();
		if(temp.getFitness() < fitness) {
			fitness = temp.getFitness();
			return temp;
			}  
		}
		return temp;
	}
	
	//insert mutation from paper
	//gets 2 random vals. inserts rvalue1 at the right of rvalue2 and pushes every element to the right
	//needs adjustments because ranges are different for color and vertices, dont think is good anyway
	//think about removing it, it will mess with the colors and positions
	public Solution insertMutation(Solution sol, int nValues) {
		int value1 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE );
		int value2 = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE );
		if(value1 > value2) {
			int temp = value2;
			value2 = value1;
			value1 = temp;
		} int tmpval = sol.getValue(value2);
		for(int i=value1; i<value2; i++) {
			sol.setValue(i+1, sol.getValue(i));
		} sol.setValue(value1+1, tmpval);
		return sol;
	}
	//inversion mutation from paper
	//Chooses 2 points and inverts the values between the points
	public Solution inversionMutation(Solution sol, int number) {
		Solution temp = sol.copy();
		int point1=5, point2=5;
		int triangleIndex;
		
		for(int j=0; j<number; j++) {
			triangleIndex = r.nextInt(100);
			while(point1==point2) {
				point1 = r.nextInt(10);
				if(point1 <4) {
					point2 = r.nextInt(4);
				} else {
					point2 = r.nextInt(6)+4;
				}
				if(point1 > point2) {
					int tem = point2;
					point2 = point1;
					point1 = tem;
				}
			}
			int[] values = new int[point2-point1];
			for(int i=point1; i<point2 ; i++) {
				values[i-point1] = temp.getValue(triangleIndex*10 + i);
				
			}
			for(int i=0; i<point2-point1; i++) {
				temp.setValue(triangleIndex*10 + point1+i, values[values.length-1-i]);
			}
		}
		return sol;
	}
	// pass true to mutate color, false to mutate triangle
	// scramble mutation doesnt change values, trying to implement it with box
	public Solution scrambleMutation(Solution sol, int nValues, boolean color, int range) {
		int valuesToMutate[] = new int[nValues];
		int valueIndex=0, triangleIndex=0;
		
		for(int i=0; i<nValues; i++) {
			triangleIndex = r.nextInt(100);
			if(color)
				valueIndex = r.nextInt(4);
			else
				valueIndex = r.nextInt(6)+4;
			
			valuesToMutate[i] = triangleIndex*10 + valueIndex;
		}
		for(int i=0; i<nValues-1; i++) {
			int val1 = sol.getValue(valuesToMutate[i]);
			int val2 = sol.getValue(valuesToMutate[i + 1]);
			int rand = r.nextInt(range);
			int tempRF = sol.getRelativeFitness(valuesToMutate[i]);
			sol.setValue(valuesToMutate[i], val2);
			sol.setRelativeFitness(valuesToMutate[i], sol.getRelativeFitness(valuesToMutate[i+1]));
			sol.setValue(valuesToMutate[i+1], val1);
			sol.setRelativeFitness(valuesToMutate[i+1], tempRF);
			}
		return sol;
		}
		
	//mutate a triangle
	public Solution trianglePosMutation(Solution sol, int triangleIndex) {
		int triang = triangleIndex;
		for(int i=4; i<Solution.VALUES_PER_TRIANGLE; i++) {
		/*	if(i<4)
				sol.values[triang*Solution.VALUES_PER_TRIANGLE+i] = r.nextInt(256);
			else */
				sol.values[triang*Solution.VALUES_PER_TRIANGLE+i] = r.nextInt(200);
		}
		return sol;
	}
	
	//mutation for N values, sets relative fitness to the closes val (from the pop) in the same index. Trying to keep relative Fitness accurate
	public Solution applyMutationNrelativeF(Solution sol, int number) {
		Solution temp = sol.copy();int count=0;
		for( ; 0<number; number--) {
			int rand;
			int index = r.nextInt(instance.getNumberOfTriangles()* Solution.VALUES_PER_TRIANGLE);
			if (index%10 < 4) {
				rand = r.nextInt(256);
				temp.values[index] = rand;
			} else {
				if (index % 2 == 0) {
					rand = r.nextInt(instance.getImageWidth() + 1);
					temp.values[index] = rand;
				} else {
					rand = r.nextInt(instance.getImageHeight() + 1);
					temp.values[index] = rand;
				}
			}
			
			int tempa = lookupValueRFitness(index, rand);
			temp.setRelativeFitness(index, tempa);
		}
		return temp;
	}
	public Solution mutateColor(Solution temp, int triangleIndex, boolean maxmin) {
		int maxI = 0; int minI = 0;
		int max = 0; int min = 300;
		for(int i=0; i<4; i++) {
			if(temp.getValue(triangleIndex*10 +i) > max ) {
				maxI = triangleIndex*10 + i;
				max = temp.getValue(triangleIndex*10 +i);
			}
			if(temp.getValue(triangleIndex*10 +i) < min) {
				minI = triangleIndex*10 + i;
				min = temp.getValue(triangleIndex*10 +i);
			}
		}
		if(maxmin)
			temp.setValue(maxI, r.nextInt(100));
		else
			temp.setValue(minI, r.nextInt(255));
		
		return temp;
	}
	public Solution boxFitnessMutation(Solution sol, int nValues, int nTimes, int range) {
		Solution temp = sol.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int valueIndex = r.nextInt(Solution.VALUES_PER_TRIANGLE);
		int change;
		double fitness = sol.getFitness(); int count=0;
		
		for(int i=0; i<nTimes; i++) {			
			for(int j=0; j<nValues; j++) {
				temp = sol.copy();
				triangleIndex = r.nextInt(instance.getNumberOfTriangles());
				valueIndex = r.nextInt(Solution.VALUES_PER_TRIANGLE);
				change = r.nextInt(range);
				if(r.nextBoolean())
					change = -change;
				//System.out.println("triying mutate  "+i+" times "+triangleIndex+"  "+valueIndex);
				if (valueIndex < 4) {
					if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
							|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
						temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
					else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255
							|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255)
						temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 255;
					else
						temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
				} else {
					if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
							|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
						temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
					else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200
							|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200)
						temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 199;
					else {
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
						if (valueIndex % 2 == 0) {
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
						} else {
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							}
					}
				} 
			} 
			count++;
			if(count==nTimes)
				return temp;
		temp.evaluate();
		if(temp.getFitness() < fitness) {
			fitness = temp.getFitness();
			return temp;
			}  
		}
		return temp;
	}
	// box mutation but it goes val by val instead of random
	public Solution boxFitnessMutationLooped(Solution sol, int nValues, int nTimes, int range) {
		Solution temp = sol.copy();
		int triangleIndex,change, valueIndex;
		double fitness = sol.getFitness(); 
		
		for(int i=0; i<nTimes; i++) {			
				temp = sol.copy();
				triangleIndex = countMutationI/10;
				valueIndex = countMutationI%10;
				change = r.nextInt(range);
					if (valueIndex < 4) {
						if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
						else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 255;
						else
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
					} else {
						if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
						else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 199;
						else {
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							if (valueIndex % 2 == 0) {
								temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							} else {
								temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							}
						}
					} /*
					count++;
					if(count==nTimes)
						return temp; */
					temp.evaluate();
					if(temp.getFitness() < fitness) {
						/*	fitness = temp.getFitness();
						return temp; */
						fitness = temp.getFitness();
						sol = temp.copy();
					}
					temp = sol.copy();
					change = -change;
					if (valueIndex < 4) {
						if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
						else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 255)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 255;
						else
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
					} else {
						if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change < 0 
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] - change < 0)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 0;
						else if(temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200
								|| temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] + change > 200)
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] = 199;
						else {
							temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							if (valueIndex % 2 == 0) {
								temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							} else {
								temp.values[triangleIndex * Solution.VALUES_PER_TRIANGLE + valueIndex] += change;
							}
						}
					} 
					count++;
					if(count==nTimes)
						return temp;
					temp.evaluate();
					if(temp.getFitness() < fitness) {
					/*	fitness = temp.getFitness();
						return temp; */
						fitness = temp.getFitness();
						sol = temp.copy();
					}
					countMutationI++;
					if(countMutationI==1000) 
						countMutationI=0;
		}
		return temp;
	}


	//                                      ************************************************
	//                                      ------------- Other methods --------------------
	//                                      ************************************************
	
	//Fitness sharing, a solution of premature convergence
	public void fitnessSharing(int k) {
		double newF;
		if(genVsPhen) {
			newF= population[k].getFitness() * (1-MinMaxGenVar(k));
			if(MinMaxGenVar(k) == 1 || MinMaxGenVar(k) == 0 )
				newF = population[k].getFitness() * 0.9;
			population[k].setSharedFitness(newF);
		}
		else {
			newF= population[k].getFitness() * (1-MinMaxPhenVar(k));
			if(MinMaxPhenVar(k) == 1 || MinMaxPhenVar(k) == 0 )
				newF = population[k].getFitness() * 0.9;
			population[k].setSharedFitness(newF);
		}
	}
	
	// Would invert only one solution, makes no sense
	public Solution Inversion(Solution sol) {
		int InitialPos, FinalPos;
		for(int i=0; i<NInversions; i++) {
			InitialPos = 0; FinalPos=9;
			while((colorVsIndex(InitialPos%10))!=(colorVsIndex(FinalPos%10))) {
				InitialPos = r.nextInt(999);
				FinalPos = r.nextInt(999);
			}
			sol.swapValues(InitialPos, FinalPos);
		}
		return sol;
	}
	// Does the SAME inversion for both parents, needed for XO and not get of the range
	public void twoInversion(int parents[]) {
		int InitialPos, FinalPos;
		for(int i=0; i<NInversions; i++) {
			InitialPos = 0; FinalPos=9;
			while((colorVsIndex(InitialPos%10))!=(colorVsIndex(FinalPos%10))) {
				//System.out.println("Initial c "+ colorVsIndex(InitialPos));
				//System.out.println("Final c "+ colorVsIndex(FinalPos));
				InitialPos = r.nextInt(999);
				FinalPos = r.nextInt(999);
			}
			population[parents[0]].swapValues(InitialPos, FinalPos);
			population[parents[1]].swapValues(InitialPos, FinalPos);
		}
	}
	//Sort the indexes of 1 solution/indv
	public Solution reorder(Solution sol) {	
		sol.sortIndexes();
		return sol;
	}
	//Sort the indexes of all indvs, of the pop
	public void reorderALL(Solution[] pop) {
		for(int i=0; i<populationSize; i++) {
			population[i].sortIndexes();
		}
	}
	//lookups specific index of inverted individual and returns its position in the array 
	public int lookupIndex(int[] indexes, int value) {
		int index = 1000;
		for(int i=0; i<1000; i++) {
			if(indexes[i] == value)
				return index;
		}
		return index;
	}
	// --------------- Replacement: P=P'
	// --- Elitism: one elit
	public Solution[] replacement(Solution[] offspring) {
		Solution bestParent = getBest(population);
		Solution bestOffspring = getBest(offspring);
		if (bestOffspring.getFitness() <= bestParent.getFitness()) {
			return offspring;
		} else {
			Solution[] newPopulation = new Solution[population.length];
			newPopulation[0] = bestParent;
			int worstOffspringIndex = getWorstIndex(offspring);
			for (int i = 0; i < newPopulation.length; i++) {
				if (i < worstOffspringIndex) {
					newPopulation[i + 1] = offspring[i];
				} else if (i > worstOffspringIndex) {
					newPopulation[i] = offspring[i];
				}
			}
			return newPopulation;
		}
	}
	// Returns pop with best "n" indv.  Receives a pop and the number of best to insert (removes worst indv)
	public Solution[] Elitism(Solution[] offsprings, int number) {
		if(activateFitnessSharing) {
			sortSolutionsSharing(offsprings);
			for(int i=0 ; 0!=number; i++, number--) {
				offsprings[i] = getBesta(population, (offsprings.length-number));
			}
			return offsprings;			
		} else {
			sortSolutions(offsprings);
			for(int i=0 ; 0!=number; i++, number--) {
				offsprings[i] = getBesta(population, (offsprings.length-number));
			}
		return offsprings;
		}
	}
	//check if color is within certain range (obtained from stats). Show slight improvement
	public Solution checkColor(Solution sol, int triangleIndex) {
		while(sumColor(sol, triangleIndex) > 830) {
			sol = mutateColor(sol, triangleIndex, true);
		} 
		while(sumColor(sol, triangleIndex) < 100) {
			sol = mutateColor(sol, triangleIndex, false);
		} 
		return sol;
	}
	//check if trianglePerimeter is within certain range (obtained from stats). Doesnt seem to be good
	public Solution checkPerimeter(Solution sol, int triangleIndex) {
		while(AvgTrianglePerimeter(sol) > 550 || 
				AvgTrianglePerimeter(sol) < 400) {
			sol = trianglePosMutation(sol, triangleIndex); 
		}
		/*
		while(trianglePerimeter(sol, triangleIndex) < 160) {
			sol = trianglePosMutation(sol, triangleIndex)
		} */
		return sol;
	}
	
	
	
	//                                      ************************************************
	//                                      --------------- Auxiliary ----------------------
	//                                      ************************************************
	
	//we create double vect fitnesses to use in the following methods of selection
	protected double[] fitnesses = new double[populationSize]; //we could put in here also the random probability
	protected double[] sharedFitnesses = new double[populationSize];
	//total fitness of pop
	protected double totalFitness() {
		double total = 0;
		if(activateFitnessSharing) {
			for (int i = 0; i < fitnesses.length; i++) {
				total += sharedFitnesses[i];
			}
		} else {
			for (int i = 0; i < fitnesses.length; i++) {
				total += fitnesses[i];
			}
		}
		return total;
	} 
	//total RelativeFitness from pop
	protected double totalRFitness() {
		double total=0;
		for(int i=0; i<populationSize; i++) {
			total += population[i].getAvgRelativeFitness();
		}
		return total;
	}
	public double avgFitness() {
		double avgFitness=0;
		for(int i=0; i<populationSize; i++) {
			avgFitness += population[i].getFitness();
		}
		avgFitness= avgFitness/populationSize;
		return avgFitness;
	}
	//needs adjustments on the formula
	public double eucledianDistance(int popindex1) { 
		double totalDistance =0;
		int[] average = avgDistance();
		for(int j=0; j<instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; j++) {
			totalDistance += Math.abs(population[popindex1].getValue(j)-average[j]);
		}
		totalDistance = totalDistance/1000;
		return totalDistance;
	}
	public int[] avgDistance() {
		int average[] = new int[1000];
		for(int i=0; i<1000; i++) {
			for(int j=0; j<populationSize; j++) {
				average[i]+=population[j].getValue(i);
			}
		}
		for(int i=0; i<1000; i++) {
			average[i] = average[i]/25;
		}
		return average;
	}
	public int avgavgDistance() {
		int average = 0; int[] avg=avgDistance();
		for(int i=0; i<1000; i++) {
			average += avg[i];
		} average = average/1000;
		return average;
	}
	public double avgeucledianDistance(int popindex) {
		double avg=0;
		for(int i=0; i<populationSize; i++) {
			avg +=eucledianDistance(popindex);
		} avg= avg/(populationSize - 1);
		return avg;
	}
	public double totalAvgEucledianDistance() {
		double avg = 0;
		for(int i=0; i<populationSize; i++) {
			avg += avgeucledianDistance(i);
		} avg = avg / (populationSize-1);
		return avg;
	}
	public double genotypicVar() {
		long var=0;
		for(int i=0; i<populationSize; i++) {
			var += Math.pow(Math.abs(eucledianDistance(i) - avgavgDistance()), 2);
		}
		var = (var) / (populationSize-1);
		return var;
	}
	public double genotypicVar1000() {
		long var=0;
		for(int i=0; i<populationSize; i++) {
			var += Math.pow(Math.abs(avgeucledianDistance(i) - totalAvgEucledianDistance()), 2) * 1000;
		}
		var = (var) / (populationSize-1);
		return var;
	}
	public double indvGenotypicVar(int popindex) {
		double var = 0;
		int var1=0; int[] avg =avgDistance();
		for(int i=0; i<1000; i++) {
			var1+=population[popindex].getValue(i) - avg[i];
		}
		var += Math.pow(Math.abs(var1 - avgavgDistance()), 2);
		return var;
	}
	//true for Max, false for Min
	public double MMindvGenotypicVar(boolean flag){
		double max=0, min=100000;
		for(int i=0; i<populationSize; i++) {
			if(indvGenotypicVar(i) > max) {
				max = indvGenotypicVar(i);
			}
			if(indvGenotypicVar(i) < min) {
				min = indvGenotypicVar(i);
			}
		}
		if(flag)
			return max;
		else
			return min;
	}
	public double MinMaxGenVar(int popindex) {
		return (indvGenotypicVar(popindex) - MMindvGenotypicVar(false)) / 
				(MMindvGenotypicVar(true) - MMindvGenotypicVar(false));
		}
	public double phenotypicVar() {
		double var=0;
		for(int i=0; i<populationSize; i++) {
			var += Math.pow(Math.abs(population[i].getFitness() - avgFitness()), 2);
		}
		var = (var*1) / (populationSize-1);
		return var;
	}
	public double indvPhenotypicVar(int popindex) {
		double var=0;
		var  = Math.pow(Math.abs(population[popindex].getFitness() - avgFitness()), 2);
		return var;
	}
	//true for Max, false for Min
	public double MMindvPhenotypicVar(boolean flag) {
		double max=0, min=100000;
		for(int i=0; i<populationSize; i++) {
			if(indvPhenotypicVar(i) > max) {
				max = indvPhenotypicVar(i);
			}
			if(indvPhenotypicVar(i) < min) {
				min = indvPhenotypicVar(i);
			}
		}
		if(flag)
			return max;
		else
			return min;
	}
	public double MinMaxPhenVar(int popindex) {
		return (indvPhenotypicVar(popindex) - MMindvPhenotypicVar(false)) / 
			(MMindvPhenotypicVar(true) - MMindvPhenotypicVar(false));
	}
	public double ZscorePhenVar(int popindex) {
		double[] phenvars = new double[populationSize];
		for(int i=0; i<populationSize; i++) {
			phenvars[i] = indvPhenotypicVar(i); 
		}
		return ((indvPhenotypicVar(popindex) - phenotypicVar()) / 
				Statistics.standardDeviation(phenvars));
	}
	
	

	// --------------- Auxiliary methods -------------------

	// returns true if its color, false if its index
	public boolean colorVsIndex(int valueIndex) {
		valueIndex = valueIndex%10;
		if (valueIndex < 4) {
			return true;
		} else {
			return false;
		}
	}
	// get best solution
	public Solution getBest(Solution[] solutions) {
		Solution best = solutions[0];
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() < best.getFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}
	//get solution for index i, 0 is the highest and 24 the lowest ordered by fitness
	public Solution getBesta(Solution[] solutions, int index) {
		sortSolutions(solutions);

		return solutions[index];
	}
	//Lookups val of parent in offspring
	//true for increase, false for decrease
	public Solution mapValueRF(Solution fp1, Solution temp, boolean increase) {
		for(int i=0; i<1000; i++) {
			if(fp1.getValue(i)==temp.getValue(i)) {
				if(increase)
					fp1.increaseRelativeFitnessIndex(i);
				else
					fp1.decreaseRelativeFitnessIndex(i);
			}
		}
		return fp1;
	}
	
	//sorts by fitness. Best one would be index 0
	public Solution[] sortSolutions(Solution[] solutions) {
		int j;
		Solution temp;
		boolean flag=true;
		while(flag) {
			flag=false;
			for(j=0; j<solutions.length-1; j++) {
				if(solutions[j].getFitness() < solutions[j+1].getFitness()) {
					temp = solutions[j]; 
					solutions[j] = solutions[j+1];
					solutions[j+1] = temp;
					flag=true;
				}
			}
		}
		return solutions;
	}
	//same but sorts with shared fitness
	public Solution[] sortSolutionsSharing(Solution[] solutions) {
		int j;
		Solution temp;
		boolean flag=true;
		while(flag) {
			flag=false;
			for(j=0; j<solutions.length-1; j++) {
				if(solutions[j].getsharedFitness() < solutions[j+1].getsharedFitness()) {
					temp = solutions[j]; 
					solutions[j] = solutions[j+1];
					solutions[j+1] = temp;
					flag=true;
				}
			}
		}
		return solutions;
	}
	//experiment. Trying to sort but position of X vertice
	public Solution sortSolutionsX(Solution solutions) {
		int j;
		int temp= 0; int tempRF=0;
		boolean flag=true;
		while(flag) {
			flag=false;
				for(int i=0; i<instance.getNumberOfTriangles()-1; i++) {
					if(solutions.getXFromVertex1(i) < solutions.getXFromVertex1(i+1)) {
						for(int a=0; a<10; a++) {
							temp = solutions.getValue(i*10 + a); 
							solutions.setValue(i*10 + a, solutions.getValue((i+1)*10 + a)); 
							solutions.setValue((i+1) * 10, temp); 
							tempRF = solutions.getValue(i*10+a);
							solutions.setRelativeFitness(i*10 + a, solutions.getRelativeFitness((i+1)*10 + a)); 
							solutions.setRelativeFitness((i+1) * 10, temp); 
							flag=true;
						}
					}
				}
			
		}
		return solutions;
	}
	// Get "centroid" for each triangle in xy coordenates
	public int[] avgCord(Solution sol, int triangle) {
		int[] XY = new int[2];
			 XY[0] +=sol.getXFromVertex1(triangle) + sol.getXFromVertex2(triangle) + sol.getXFromVertex3(triangle);
			 XY[1] +=sol.getYFromVertex1(triangle) + sol.getYFromVertex2(triangle) + sol.getYFromVertex3(triangle);
		
		XY[0] =XY[0]/3; XY[1] = XY[1] /3 ;
		
		return XY;
	}
	//returns the Quadrant(16 total) that the triangle is located
	//ex: Quadr 12 is x<50 and y<50
	public int cordQuadrant(Solution sol, int triangleIndex) {
		int count[] = new int[16];
		int XY[] = new int[2];
			XY = avgCord(sol, triangleIndex);
			if(XY[0] <50) {
				if(XY[1] < 50)
					return 12;
				else if(XY[1] < 100)
					return 8;
				else if(XY[1] < 150)
					return 4;
				else
					return 0;
			}else if(XY[0] <100){
				if(XY[1] < 50)
					return 13;
				else if(XY[1] < 100)
					return 9;
				else if(XY[1] < 150)
					return 5;
				else
					return 1;
			} else if(XY[0] <100){
				if(XY[1] < 50)
					return 14;
				else if(XY[1] < 100)
					return 10;
				else if(XY[1] < 150)
					return 6;
				else
					return 2;
			}else {
				if(XY[1] < 50)
					return 15;
				else if(XY[1] < 100)
					return 11;
				else if(XY[1] < 150)
					return 7;
				else
					return 3;
			}
	}
	//Sorts the triangles(inside the Indv) by location of Quadr
	public Solution sortByQuadr(Solution sol) {
		int j;
		int[] temp = new int[10];
		boolean flag=true;
		while(flag) {
			flag=false;
			for(j=0; j<99; j++) {
				if(cordQuadrant(sol, j) > cordQuadrant(sol, j+1)) {
					temp= sol.getTriangle(j);
					sol.setTriangle(j, sol.getTriangle(j+1));
					sol.setTriangle(j+1, temp);
					flag=true;
				}
			} 
			
		} 
		return sol;
	}
	//Checks Cordenates are within certain parameters obtained from stats
	//Doesnt seem to work, probly because the Solution cant get to the "optimal" stats if its conditioned from the start
	public Solution checkCord(Solution sol) {
		int[] coordenates = new int[2];
		
		for(int i=0; i<100; i++) {
			coordenates = avgCord(sol, i);
			while(coordenates[0] > 100 && coordenates[0] < 150) {
				sol = trianglePosMutation(sol, i);
				coordenates = avgCord(sol, i);
				}
			for(int j=4; j<10; j++) {
				lookupValueRFitness(i*10 + j, sol.getValue(i*10 + j));
				sol.setRelativeFitness(i*10 + j ,sol.getRelativeFitness(i*10 + j));
				}
			}
			
		return sol;
	}
	//expirement, trying to organize the triangles before XO
	public Solution organizeCordn(Solution solutions) {
		int tem=0;
		int temp[] = new int[10];
		int tempRF[] = new int[10];
		boolean flag=true;
		int[][] XY = new int[100][2];
		for(int i=0; i<instance.getNumberOfTriangles(); i++) {
			XY[i] = avgCord(solutions, i);
		}
		while(flag) {
			flag=false;
				for(int i=0; i<instance.getNumberOfTriangles()-1; i++) {
						if(XY[i][0] < XY[i+1][0]) {
							temp = solutions.getTriangle(i); 
							tem = XY[i][0];
							solutions.setTriangle(i, solutions.getTriangle(i+1));
							XY[i][0] = XY[i+1][0];
							solutions.setTriangle(i+1, temp);
							XY[i+1][0] = tem;
							
							tempRF = solutions.getTriangleRF(i);
							solutions.setTriangleRF(i, solutions.getTriangleRF(i+1)); 
							solutions.setTriangleRF(i+1, tempRF); 
							flag=true;
					}
					System.out.println("XY INIT "+i +"   "+Arrays.toString(avgCord(solutions, i)));
				}
		}
		for(int i=0; i<100; i++) {
			System.out.println("XY INIT "+i +"   "+Arrays.toString(avgCord(solutions, i)));
		}

		return solutions;
	}
	
	// get best worst
	public int getWorstIndex(Solution[] solutions) {
		Solution worst = solutions[0];
		int index = 0;
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() > worst.getFitness()) {
				worst = solutions[i];
				index = i;
			}
		}
		return index;
	}

	//difference between solutions
	//needs tweaks on formula
	public double rateSimilarity(int parents[]) {
		double difference =0;
		for(int i=0; i<instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			difference+=Math.abs( population[parents[0]].getValue(i) - population[parents[1]].getValue(i));
		}
		return (difference/(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE));
	}
	//same for triangle
	public double rateSimilarityTriangle(int parents[], int triangleIndex) {
		double difference =0;
		for(int i=0; i<Solution.VALUES_PER_TRIANGLE; i++) {
			difference+=Math.abs( population[parents[0]].getValue(i*triangleIndex) - population[parents[1]].getValue(i*triangleIndex));
		}
		return (difference/(Solution.VALUES_PER_TRIANGLE));
	}
	//same for value
	public int rateSimilarityValue(int parents[], int indexValue) {
		int difference = 0;
		difference+=Math.abs( population[parents[0]].getValue(indexValue) - population[parents[1]].getValue(indexValue));
		
		return difference ;
	}
	// update output
	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());
		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}
	}
	// lookup Relative Fitness for closer value in index X for value passed as parameter
	public int lookupValueRFitness(int index, int value) {
		int closerVal=1000; int rFitness = 0;
		index = index%10;
		for(int i=0; i<populationSize; i++) {
			for(int j=0; j<100; j++) {
				
				if(Math.abs(population[i].getValue(j*10 + index) - value) < closerVal) {
					closerVal = Math.abs(population[i].getValue(j*10 + index) - value);
					rFitness = population[i].getRelativeFitness(j*10 + index);
				}
			}
		}
		return rFitness;
	}
	public double avgColor(Solution sol,int triangleIndex) {
		double avg = 0;
		for(int i=0; i<4; i++) {
			avg += sol.getValue(triangleIndex *10 +i);
		}
		return avg/4;
	}
	public int sumColor(Solution sol, int triangleIndex) {
		int sum=0;
		for(int i=0; i<4; i++) {
			sum += sol.getValue(triangleIndex *10 +i);
		}
		return sum;
	}
	public double trianglePerimeter(Solution sol, int triangleIndex) {
		double p1=0, p2=0, p3=0;
		triangleIndex = triangleIndex *10;
		p1 = Math.sqrt(Math.pow(sol.getValue(triangleIndex + 4),2) + Math.pow(sol.getValue(triangleIndex + 5),2));
		p2 = Math.sqrt(Math.pow(sol.getValue(triangleIndex + 6),2) + Math.pow(sol.getValue(triangleIndex + 7),2));
		p3 = Math.sqrt(Math.pow(sol.getValue(triangleIndex + 8),2) + Math.pow(sol.getValue(triangleIndex + 9),2));
		double perimeter = p1+p2+p3;
		return perimeter;
	}
	public double AvgTrianglePerimeter(Solution sol) {
		double avg=0;
		for(int i=0; i<100; i++) {
			avg+= trianglePerimeter(sol, i);
		}
		return avg/100;
	}
	//                                      ************************************************
	//                                      ---------------- WRITE -------------------------
	//                                      ************************************************
	
	//print for Excel, comma delimited
	public void printExcTeste() {
		BufferedWriter bw = null;
		try {
			bw =  new BufferedWriter(new FileWriter("outputWvars.csv", true));
			
			if(currentGeneration==runtoWrite){
				bw.newLine();
				bw.write((Main.currentRun+1)+","+selectionTypeP1+","+selectionTypeP2+","+tournamentSize+","+crossoverType+","
						+crossoverProbability+","+kpointsXO+","+mutationProbability+","+mutationNumberPerSol+","+
						nElites+","+activateInversion+","+NInversions+","+ activateRestrictedMating+","+rateSimiliratyP
						+","+activateFitnessSharing+","+changeSelectionDuringRun+","+changeXOduringRun
						+","+changeMutationDuringRun+","+notas+","+gen0);
			} 		
			/*
			bw.write(getBest(population).getFitness()+","+avgFitness()
			+","+population[getWorstIndex(population)].getFitness());   */
			bw.append(","+getBest(population).getFitness());   /*+", "+genotypicVar()+", "+
					phenotypicVar()); */
			
		} catch(IOException e) {}
		finally {
			try {
			bw.close();
			} catch(IOException e2) {}
		}
	}
	public void printExcTesteCOUNTS() {
		BufferedWriter bw = null;
		try {
			bw =  new BufferedWriter(new FileWriter(("last"+FileNameWrite), true));
			if(currentGeneration==runtoWrite){
				bw.newLine();
				bw.write("NEW RUN");
				
			} 	
				//bw.write("GENERATION, "+ currentGeneration + ", BEST FITNESS , "+getBest(population).getFitness());
				bw.newLine();
				for(int i=0; i<1000; i++) {
					bw.write(getBest(population).getValue(i)+", ");
			}
				/*	bw.newLine();			
				bw.newLine();
			bw.write(getBest(population).getFitness()+","+avgFitness()
			+","+population[getWorstIndex(population)].getFitness());   */
			
		} catch(IOException e) {}
		finally {
			try {
			bw.close();
			} catch(IOException e2) {}
		}
	}
	//Print every 100 gens for good format
	public void Stats_Report() {
		BufferedWriter bw = null;
		try {
			bw =  new BufferedWriter(new FileWriter(FileNameWrite, true));
			double []fitnesss = new double[25];
			for(int i=0; i<populationSize; i++) {
				fitnesss[i] = population[i].getFitness();
			}
			if(currentGeneration==runtoWrite){
				bw.newLine();
			//	bw.write("NEW RUN");
				bw.write((Main.currentRun+1)+","+selectionTypeP1+","+selectionTypeP2+","+tournamentSize+","+crossoverType+","
						+crossoverProbability+","+kpointsXO+","+mutationProbability+","+mutationType+","+mutationNumberPerSol+","+
						nElites+","+activateInversion+","+NInversions+","+ activateRestrictedMating+","+rateSimiliratyP
						+","+activateFitnessSharing+","+genVsPhen+","+changeSelectionDuringRun+","+changeXOduringRun
						+","+changeMutationDuringRun+","+nEvaluates+", "+notas+","+Instant.now()+","+gen0);
			
				
			} 	
				//bw.write("GENERATION, "+ currentGeneration + ", BEST FITNESS , "+getBest(population).getFitness());
			bw.append(","+getBest(population).getFitness()+", "+ population[getWorstIndex(population)].getFitness()
					+", "+Statistics.median(fitnesss)+"," + 
				genotypicVar()+","+genotypicVar1000()+", "+phenotypicVar());
			
				/*	bw.newLine();			
				bw.newLine();
			bw.write(getBest(population).getFitness()+","+avgFitness()
			+","+population[getWorstIndex(population)].getFitness());   */
			if(currentGeneration > 1999)
				bw.append(","+Instant.now());
		} catch(IOException e) {}
		finally {
			try {
			bw.close();
			} catch(IOException e2) {}
		}
	}
	//print for notepad
		public void printTxt() {
			BufferedWriter bw = null;
			double std=0;
			try {
				bw =  new BufferedWriter(new FileWriter(FileNameWrite, true));
				
				if(currentGeneration==runtoWrite) {
					bw.newLine();
					bw.newLine();
					bw.write("\t\t*****    NEW RUN   *****");
					bw.newLine();
					bw.write(" \t---Parameter---");
					bw.newLine(); 			// One write or the other, depending on the type of mut used
					//bw.write("Mutation -->   mutationProbIndv: "+mutationProbIndv+" mutationProbTriangle: "+mutationProbTriangle+
					//		" mutationProbValue: "+mutationProbValue+" mutationProbControled: "+mutationProbControled);
					bw.write("Mutation --> "+mutationProbability+"   mutated values  "+mutationNumberPerSol);
					bw.newLine();
					bw.write("Type of XO: "+crossoverType+"  Crossover prob: "+crossoverProbability+
							" Crossover K points "+kpointsXO);bw.newLine();
					bw.write("Selection type: p1-"+selectionTypeP1+" p2-"+selectionTypeP2);bw.newLine();
					bw.write("Number of inversions" + NInversions);
					bw.write("    Elitism: "+nElites);bw.newLine();
					bw.write("Tournament size: "+tournamentSize);bw.newLine();
					bw.write("Inversion activated "+activateInversion);bw.newLine();
					bw.write("Favorite son activated "+applyFavSon); bw.newLine();
					bw.write("Notas:  "+notas);bw.newLine();
				}
				/*
				bw.write("   Generation: "+(currentGeneration)+"  -->  Best Solution:  F- "+getBest(population).getFitness()+"  --  ");
				bw.write("Worst: F- "+population[getWorstIndex(population)].getFitness()+" -- ");
				int[][] i1 = new int[100][populationSize];
				for(int i=0; i<100; i++) {
					for(int j=0; j<populationSize; j++) {
					i1[i][j] = population[j].getValue(i);
					}
				}
				for(int i=0; i<100; i++) {
					if(i%10==0)
						bw.write("Triangle: "+i/10);
					//bw.write(" Index: "+i+" --> " + "Min: "+Statistics.min(i1[i])+" Mean "+Statistics.mean(i1[i])
					//		+"Stdev: "+Statistics.standardDeviation(i1[i])+" Max: "+Statistics.max(i1[i]));
					std += Statistics.standardDeviation(i1[i]);
				}
				bw.write("Total std: "+std);
				*/

				bw.write("Generation: "+currentGeneration+" Best Fitness: "+getBest(population).getFitness()
						+"  Average Fitness: "+avgFitness()
				+"  Worst Fitness: "+population[getWorstIndex(population)].getFitness());
				
				bw.newLine();
			} catch(IOException e) {}
			finally {
				try {
				bw.close();
				} catch(IOException e2) {}
			}
		}
		public void readValues() {
			String Path = "C:\\Users\\Passao\\Documents\\uni\\Big data\\Project CIFO2017\\";
	        String fileName= "importable.csv";
	        File file= new File(fileName);

	        // this gives you a 2-dimensional array of strings
	        List<List<String>> lines = new ArrayList<>();
	        Scanner inputStream;

	        try{
	            inputStream = new Scanner(file);

	            while(inputStream.hasNext()){
	                String line= inputStream.next();
	                String[] values = line.split(",");
	                // this adds the currently parsed line to the 2-dimensional string array
	                lines.add(Arrays.asList(values));
	            }

	            inputStream.close();
	        }catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }

	        // the following code lets you iterate through the 2-dimensional array
	        int popIndex = 0;
	        	
	        for(List<String> line: lines) {
	            int index = 0;
	            for (String value: line) {
	            	try {
	                population[popIndex].setValue(index, Integer.parseInt(value));
	                index++;
	            		} catch (NumberFormatException ex) {}
	            	}
	            System.out.println("Pop "+popIndex+"  is set ");
	            popIndex++;
	            if(popIndex>=25)
	            	break;
	        }
	     }
		
		
	
}