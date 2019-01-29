package project;

import java.util.Arrays;

public class Statistics {

	public static void print(int[] solution) {
		for (int i = 0; i < solution.length; i++) {
			System.out.print(solution[i]+", ");
		}
		System.out.println();
	}
	
	public static double sum(double[] sample) {
		double sum = 0.0;
		for (int i = 0; i < sample.length; i++) {
			sum += sample[i];
		}
		return sum;
	}
	public static double sum(int[] sample) {
		double sum = 0.0;
		for (int i = 0; i < sample.length; i++) {
			sum += sample[i];
		}
		return sum;
	}

	public static double mean(double[] sample) {
		return sum(sample) / sample.length;
	}
	public static double median(double[] sample) {
		Arrays.sort(sample);
		int point = sample.length/2;
		return sample[point];
	}

	public static double standardDeviation(double[] sample) {
		double mean = mean(sample);
		double temp = 0.0;
		for (int i = 0; i < sample.length; i++) {
			temp += Math.pow((sample[i] - mean), 2.0);
		}
		return Math.sqrt(temp / sample.length);
	}


	public static double max(double[] sample) {
		double max = sample[0];
		for (int i = 0; i < sample.length; i++) {
			if (sample[i] > max) {
				max = sample[i];
			}
		}
		return max;
	}

	public static double min(double[] sample) {
		double min = sample[0];
		for (int i = 0; i < sample.length; i++) {
			if (sample[i] < min) {
				min = sample[i];
			}
		}
		return min;
	}
	public static double diversity(int[] sample) {
		int diversity=0;
		for(int i =0; i<1000; i++) {
			
		}
		
		return 0;
	}
}
