import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.Random;

/**
 * Created by Carl Glahn on 3/5/2016.
 */
public class NN {

    public double[][][] weights;

    double[][] nodes;
    public int test;
    public double learnRate;
    int numEpochs;
    double sampleError;
    double[] epochErrors;

    public NN(int[] array){
        test = 0; //assume we are training;
        learnRate =.2;
        numEpochs = 5;
        sampleError = 0;


        //need to dynamically allocate space as we have an uneven array
        weights = new double[array.length-1][][];
        nodes = new double[array.length][];
        for(int i = 1; i<array.length; i++){ //dont make stuff for the input layer
            weights[i-1] = new double[array[i-1]+1][array[i]]; //no +1 for the final layer

        }
        for(int i = 0; i <array.length; i++){
            if(i == array.length -1){
                nodes[i]= new double [array[i]]; //no added node for output layer
            }
            else{
                nodes[i]= new double [array[i]+1];
            }
        }

        // setting the random weights
        weights = initWeights();

        //now we have to populate the nn
        /*
        If you change this you must go and manually set the w0 node to 1
        */
        for( int i = 0; i < nodes.length;i++){
            Arrays.fill(nodes[i], 1); //set to 0
        }

    }
    public double[][][] initWeights(){
        double r;
        double sign; //is the weight negative or positive
        for(int i = 0; i<weights.length; i++) {
            for(int j = 0; j < weights[i].length; j++) {
                for (int k = 0; k < weights[i][j].length ; k++) {
                    if(Math.random() > .5){
                        sign = 1; //weight will be positive
                    }
                    else{
                        sign = -1;
                    }
                    r = Math.random()/2 * sign;
                    weights[i][j][k] = r;

                }
            }
        }

        return weights;
    }

    public void printWeights()
    {
        try {
            PrintWriter writer = new PrintWriter("JettCarlWeights.txt", "UTF-8");
            for(double [][] a : weights)
            {
                for(double[] b : a)
                {
                    for(double c : b)
                    {
                        writer.println(c);
                    }
                }
            }
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found");
        }
        catch (UnsupportedEncodingException e)
        {

        }
    }

    public void setWeights()
    {
        String fileName = "JettCarlWeights.txt";

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            for(int i=0; i< weights.length;i++){
                for(int j=0; j<weights[i].length;j++){
                    for(int k=0; k<weights[i][j].length;k++){
                        line = bufferedReader.readLine();
                        if(line!=null) {
                            weights[i][j][k] = Double.parseDouble(line);
                        }
                    }
                }
            }
            while((line = bufferedReader.readLine()) != null) {
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
    }


}
