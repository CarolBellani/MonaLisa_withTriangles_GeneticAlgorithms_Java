package project;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Random;

public class Solution {

	public static final int VALUES_PER_TRIANGLE = 10;
	protected Problem instance;
	protected int[] values;
	protected double fitness;
	protected Random r;
	protected GeneticAlgorithm ga;
	public double sharedFitness;

	public Solution(Problem instance) {
		this.instance = instance;
		r = new Random();
		initialize();
	}

	public void initialize() {
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE * 3];

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			// initialize HSB and Alpha
			for (int i = 0; i < 4; i++) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			}
			// initialize vertices
			for (int i = 4; i <= 8; i += 2) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
				values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);
			}    
		}
		
		for(int i=VALUES_PER_TRIANGLE*instance.getNumberOfTriangles(); i<values.length; i++) {
			values[i] = r.nextInt(1000)+200;
		}
		for(int i=2000; i<3000; i++) {
			values[i] = i - 2000;
		}
			
	}

	public void evaluate() {
		BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[generatedImage.getWidth() * generatedImage.getHeight()];
		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, generatedImage.getWidth(), generatedImage.getHeight(),
				generatedPixels, 0, generatedImage.getWidth());
		
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] targetPixels = instance.getTargetPixels();
		long sum = 0;
		for (int i = 0; i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			int blue = (c1 & 0xff) - (c2 & 0xff);
			sum += red * red + green * green + blue * blue;
		}
		
		
		fitness = Math.sqrt(sum);
	}


	public Solution applyMutation() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		int rand;
		if (valueIndex < 4) {
			rand = r.nextInt(256);
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = rand;
		} else {
			if (valueIndex % 2 == 0) {
				rand = r.nextInt(instance.getImageWidth() + 1);
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = rand;
			} else {
				rand = r.nextInt(instance.getImageWidth() + 1);
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = rand;
			}
		}
		temp.setRelativeFitness( triangleIndex * VALUES_PER_TRIANGLE + valueIndex, 500);
		return temp;
	}
	public Solution applyMutationN(int number) {
		Solution temp = this.copy();
		for( ; 0<number; number--) {
			int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
			int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
			//System.out.println("Sys  "+ number);
			if (valueIndex < 4) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
			} else {
				if (valueIndex % 2 == 0) {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
				} else {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageHeight() + 1);
				}
			}
		}
		return temp;
	}
	
	public Solution boxMutation(int range, int nValues) {
		Solution temp = this.copy();
		if(nValues==0) {
			for(int i=0; i<instance.numberOfTriangles*VALUES_PER_TRIANGLE; i++) {
				int a=r.nextInt(range);
				if(i%10<4) {
					if(temp.values[i]+a>255)
						temp.values[i] =255;
					else if(temp.values[i]-a<0)
						temp.values[i] = 0;
					else {
						if(r.nextBoolean()) {
							temp.values[i] += a;						
						} else
							temp.values[i] -= a;
					}
				} else {
					if(temp.values[i]+a>200)
						temp.values[i] =199;
					else if(temp.values[i]-a<0)
						temp.values[i] = 0;
					else {
						if(r.nextBoolean()) {
							temp.values[i] += a;						
						} else
							temp.values[i] -= a;
					}
				}
			}
		return temp;
		}
		else {
			for(int j=0; j<nValues; j++) {
			int a=r.nextInt(range); int i=r.nextInt(range);
			if(i%10<4) {
				if(temp.values[i]+a>255)
					temp.values[i] =255;
				else if(temp.values[i]-a<0)
					temp.values[i] = 0;
				else {
					if(r.nextBoolean()) {
						temp.values[i] += a;						
					} else
						temp.values[i] -= a;
				}
			} else {
				if(temp.values[i]+a>200)
					temp.values[i] =199;
				else if(temp.values[i]-a<0)
					temp.values[i] = 0;
				else {
					if(r.nextBoolean()) {
						temp.values[i] += a;						
					} else
						temp.values[i] -= a;
				}
			}
		}
		return temp;
		}
	}
	

	public Solution applyMutationAll() {
		Solution temp = this.copy();
		for(int i=0; i<(instance.getNumberOfTriangles()); i++) {
			for(int j=0; j<VALUES_PER_TRIANGLE; j++)
				if(Main.mutationEachValue>r.nextDouble()) {
				if ( j < 4) {
					temp.values[i * VALUES_PER_TRIANGLE + j] = r.nextInt(256);
				} else {
					if (j % 2 == 0) {
						temp.values[i * VALUES_PER_TRIANGLE + j] = r.nextInt(instance.getImageWidth() + 1);
					} else {
						temp.values[i * VALUES_PER_TRIANGLE + j] = r
								.nextInt(instance.getImageHeight() + 1);
					}
				}
			}
		}
		return temp;
	}
	public Solution mutateColor(int triangleIndex, boolean maxmin) {
		Solution temp = this.copy();
		int maxI = 0; int minI = 0;
		int max = 0; int min = 300;
		for(int i=0; i<4; i++) {
			if(temp.values[triangleIndex*10 + i] > max ) {
				maxI = triangleIndex*10 + i;
				max = temp.values[triangleIndex*10 + i];
			}
			if(temp.values[triangleIndex*10 + i] < min) {
				minI = triangleIndex*10 + i;
				min = temp.values[triangleIndex*10 + i];
				System.out.println("min "+min + "  minI"+minI);
				System.out.println(temp.values[triangleIndex*10 + i]);
			}
		}
		System.out.println("min "+temp.values[minI]);
		System.out.println("max "+temp.values[maxI]);
		System.out.println("NEW");
		if(maxmin)
			temp.values[maxI] = r.nextInt(255);
		else
			temp.values[minI] = r.nextInt(255);
		
		System.out.println("min "+temp.values[minI]);
		System.out.println("max "+temp.values[maxI]);
		return temp;
	}
	public void draw() {
		BufferedImage generatedImage = createImage();
		Graphics g = Problem.view.getFittestDrawingView().getMainPanel().getGraphics();
		g.drawImage(generatedImage, 0, 0, Problem.view.getFittestDrawingView());
	}

	public void print() {
		System.out.printf("Fitness: %.1f\n", fitness);
	}

	public int getValue(int index) {
		return values[index];
	}

	public void setValue(int index, int value) {
		values[index] = value;
	}
	public void setValueIndex(int index, int value, int newIndex) {
		values[index] = value;
		//setIndex(index, newIndex);
		values[2000+index] = newIndex;
	}

	public int getHue(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 0];
	}

	public int getSaturation(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 1];
	}

	public int getBrightness(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 2];
	}

	public int getAlpha(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 3];
	}

	public int getXFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 4];
	}

	public int getYFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 5];
	}

	public int getXFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 6];
	}

	public int getYFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 7];
	}

	public int getXFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 8];
	}

	public int getYFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 9];
	}

	public void setHue(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 0] = value;
	}

	public void setSaturation(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 1] = value;
	}

	public void setBrightness(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 2] = value;
	}

	public void setAlpha(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 3] = value;
	}

	public void setXFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 4] = value;
	}

	public void setYFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 5] = value;
	}

	public void setXFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 6] = value;
	}

	public void setYFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 7] = value;
	}

	public void setXFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 8] = value;
	}

	public void setYFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 9] = value;
	}

	public int[] getVertex1(int triangleIndex) {
		return new int[] { getXFromVertex1(triangleIndex), getYFromVertex1(triangleIndex) };
	}

	public int[] getVertex2(int triangleIndex) {
		return new int[] { getXFromVertex2(triangleIndex), getYFromVertex2(triangleIndex) };
	}

	public int[] getVertex3(int triangleIndex) {
		return new int[] { getXFromVertex3(triangleIndex), getYFromVertex3(triangleIndex) };
	}

	public Problem getInstance() {
		return instance;
	}

	public int[] getValues() {
		return values;
	}
	public int[] getTriangle(int triangleIndex) {
		int[] val = new int[10];  
		for(int i=0; i<10; i++) {
			val[i] = values[triangleIndex*10 +i];
		}
		return val;
	}
	public void setTriangle(int triangleIndex, int[] val) {
		for(int i=0; i<10; i++) {
			values[triangleIndex*10 +i] = val[i] ;
		}
	}
	public int[] getTriangleRF(int triangleIndex) {
		int[] val = new int[10];  
		for(int i=0; i<10; i++) {
			val[i] = values[1000+triangleIndex*10 +i];
		}
		return val;
	}
	public void setTriangleRF(int triangleIndex, int[] val) {
		for(int i=0; i<10; i++) {
			values[1000+triangleIndex*10 +i] = val[i];
		}
	}

	public double getFitness() {
		return fitness;
	}
	public double getsharedFitness() {
		return sharedFitness;
	}
	public void setSharedFitness(double value) {
		sharedFitness = value;
	}
	public Solution copy() {
		Solution temp = new Solution(instance);
		for (int i = 0; i < values.length; i++) {
			temp.values[i] = values[i];
		}
		temp.fitness = fitness;
		return temp;
	}
	public int getRelativeFitness(int index) {
		return values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + index];
	}
	public void setRelativeFitness(int index, int value) {
		values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + index] = value;
	}
	public int getIndex(int index) {
		return values[2000+ index];
	}
	public void setIndex(int initialIndex, int finalIndex) {
		values[2000 + initialIndex] = finalIndex;
	}
	public void swapValues(int initialIndex, int finalIndex) {
		int temp = getValue(initialIndex);
		setValue(initialIndex, getValue(finalIndex));
		setValue(finalIndex, temp);
		int tempIndex = getIndex(initialIndex);
		setIndex(initialIndex, getIndex(finalIndex));
		setIndex(finalIndex, tempIndex);
	}
	public void sortIndexes() {
		int j;
		boolean flag=true;
		while(flag) {
			flag=false;
			/*
			for(int i=0; i<1000; i++) {
				System.out.print(getIndex(i)+", ");
			}
			System.out.println();   */
			for(j=0; j<1000-1; j++) {
				if(getIndex(j) > getIndex(j+1)) {
					swapValues(j, j+1);
					flag=true;
					//System.out.println("1 --> "+getIndex(j)+"  2--> "+getIndex(j+1));
				}
			}
		}
	}
	public int getWorstRelativeFitness() {
		int worst = 9999999;
		int pos = 0;
		for(int i=0; i<instance.numberOfTriangles*VALUES_PER_TRIANGLE; i++) {
			if(getRelativeFitness(i) < worst) {
				worst=getRelativeFitness(i);
				pos = i;
			}
		}
		return pos;
	}
	public void increaseRelativeFitness() {
		for(int i=0; i<instance.numberOfTriangles*VALUES_PER_TRIANGLE ; i++) {
			values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + i] += 10;
		}
	}
	public void increaseRelativeFitnessIndex(int index) {
		
		values[1000+index] += 10;
	}
	public void decreaseRelativeFitness() {
		for(int i=0; i<instance.numberOfTriangles*VALUES_PER_TRIANGLE ; i++) {
			if(values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + i] < 15)
				values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + i] = 1;
			else
				values[instance.numberOfTriangles*VALUES_PER_TRIANGLE + i] -= 10;
		}
	}
	public void decreaseRelativeFitnessIndex(int index) {
		if(values[1000+index] < 11)
			values[1000+index] = 1;
		values[1000+index] -= 10;
	}
	public void normalizeRelativeFitness() {
		for(int i=0; i<1000; i++) {
			setRelativeFitness(i, getRelativeFitness(i)/1000);
		}
	}
	public double getAvgRelativeFitness() {
		double sum=0;
		for(int i=0; i<instance.numberOfTriangles*VALUES_PER_TRIANGLE ; i++) {
			sum+= getRelativeFitness(i);
		}
		return sum/1000;
	}

	// get avg de 100, index 4==300-400
	public double getAvgCemRelativeFitness(int index) {
		double sum = 0;
		for(int i=index*100; i<(index*100+100); i++) {
			sum+=getRelativeFitness(i);
		}
		return sum/100;
	}

	

	private BufferedImage createImage() {
		BufferedImage target = instance.getTargetImage();
		BufferedImage generatedImage = new BufferedImage(target.getWidth(), target.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics generatedGraphics = generatedImage.getGraphics();

		generatedGraphics.setColor(Color.GRAY);
		generatedGraphics.fillRect(0, 0, generatedImage.getWidth(), generatedImage.getHeight());
		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			generatedGraphics.setColor(expressColor(triangleIndex));
			generatedGraphics.fillPolygon(expressPolygon(triangleIndex));
		}
		return generatedImage;
	}

	private Color expressColor(int triangleIndex) {
		int hue = getHue(triangleIndex);
		int saturation = getSaturation(triangleIndex);
		int brightness = getBrightness(triangleIndex);
		int alpha = getAlpha(triangleIndex);
		Color c = Color.getHSBColor(hue / 255.0f, saturation / 255.0f, brightness / 255.0f);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private Polygon expressPolygon(int triangleIndex) {
		int[] xs = new int[] { getXFromVertex1(triangleIndex), getXFromVertex2(triangleIndex),
				getXFromVertex3(triangleIndex) };
		int[] ys = new int[] { getYFromVertex1(triangleIndex), getYFromVertex2(triangleIndex),
				getYFromVertex3(triangleIndex) };
		return new Polygon(xs, ys, 3);
	}
}
