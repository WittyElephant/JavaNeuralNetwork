import com.sun.deploy.util.ArrayUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Carl Glahn on 3/4/2016.
 */
public class ANN {
    /* the battle plan
    1)encode input and output (218 males, 55 females)
    2)set up architecture (node class and network)
    3) check you can runa ll the way through
    4) implement backporpogation
    5)run multiple epochs
    6) cross validation
     */
    public static void main(String [] args) {
        System.out.println(System.getProperty("user.dir"));
        double[][] Data;
        boolean train = false;
        if(args.length > 0) {
            if (args[0].equals("-train")) {
                train = true;
            }
        }
        Data = train ? trainData():testData(); //Sets data to either training or testing data
        //now we create the NN
        int[] architecture = new int[]{15361, 40, 1};
        NN nn = new NN(architecture);
        if(train)
        {
            train(Data,nn);        //print these weights to the file
        }
        //} //would end the else to the if testing statement at line 49

        else if (args[0].equals("-test")){
            nn.setWeights();
            for(int i = 0; i<40; i++)
            {
                feedForward(nn, Data[i],-1);
                double tmp = nn.nodes[nn.nodes.length-1][0];
                if(tmp >.5)
                {
                    System.out.println("MALE " + (tmp*100));
                }
                else
                {
                    System.out.println("FEMALE " + ((1-tmp)*100));
                }
            }
            /*
            for each sample
                Feed forward that sample (label is = -1)
                if nn.nodes[nn.nodes.length][0] >.5
                    print man and print confidence
                else
                    print woman and confidence

             */
        }
    }

    public static void train(double[][] Data, NN nn)
    {
        //make the output, also populating my index array
        int[] maleIndexes = new int[218];
        int[] femaleIndexes = new int[55];
        // int[] finalTestIndexes = new int[Data.length];
        int[] labels = new int[Data.length];
        for (int i = 0; i < Data.length; i++) {
            //5, 16, 17, 18
            if (i < 218) {
                maleIndexes[i] = i;
                labels[i] = 1;
            } else {
                femaleIndexes[i - maleIndexes.length] = i;
                labels[i] = 0;
            }
            //finalTestIndexes[i] =i;
        }

        //now we set up for cross validation
        double[] cvErrors = new double[5]; // we have 5 folds
        int[] sectionLengths = new int[]{55, 55, 55, 55, 53};
        int[][] sections = new int[sectionLengths.length][];
        int maleCount;
        int femaleCount;
        double mean;
        double stdDev;


        // now we are ready to throw into ANN
        for (int k = 0; k < 10; k++) {  //we do the CV 10 times


            maleIndexes = randomizer(maleIndexes);
            femaleIndexes = randomizer(femaleIndexes);


            maleCount = 0;
            femaleCount = 0;
            for (int i = 0; i < sectionLengths.length; i++) {

                sections[i] = new int[sectionLengths[i]]; //allocate space

                for (int j = 0; j < sectionLengths[i]; j++) {
                    if (j < 11) {
                        sections[i][j] = femaleIndexes[femaleCount];
                        femaleCount++;
                    } else {
                        sections[i][j] = maleIndexes[maleCount];
                        maleCount++;
                    }
                }

                //we have evenly distributed the females into each section but now we must shuffle again because all the females are grouped
                sections[i] = randomizer(sections[i]);
            }


            //now to train and test
            for (int i = 0; i < sections.length; i++) { //for each section
                nn = train(nn, Data, labels, sections, i); //train it
                cvErrors[i] = test(nn, Data, labels, sections[i], 0);//test it and store the cv error
                nn.initWeights(); //rerandomize weights for next run
            }

            System.out.println("Our CV accuracies are:");
            double sum = 0;
            for (int i = 0; i < cvErrors.length; i++) { //for each section
                cvErrors[i] = 1 - cvErrors[i];   //convert to accuracies instead of error
                System.out.println(cvErrors[i]); //print out its cv accuracy
                sum += cvErrors[i];
            }
            mean = sum / cvErrors.length;  //find the mean

            //now find the std dev
            sum = 0;
            for (int i = 0; i < cvErrors.length; i++) { //for each section
                sum += Math.pow(cvErrors[i] - mean, 2);
            }
            stdDev = Math.pow(sum / cvErrors.length, .5);  //assign std dev
            System.out.println("Our mean and standard deviation of our accuracies for this cross validation are:");
            System.out.println(mean + " " + stdDev);
        }

        //now for the true test run

        train(nn, Data, labels, sections, -1); // the -1 will ensure that all sections get trained on, we will use the las randomization
    }
    public static void weightmap(NN nn){

        ArrayList<String> noteables = new ArrayList<String>();

        //find the heavy hitters
        for(int i = 0; i<nn.weights.length; i++) {
            for(int j = 0; j < nn.weights[i].length; j++) {
                for (int k = 0; k < nn.weights[i][j].length ; k++) {
                    if(i ==0 && nn.weights[i][j][k] >=1){
                        noteables.add("weight " + k + "of node "+ j + "of layer " + i);

                    }
                    else if(nn.weights[i][j][k] >=2){
                        noteables.add("weight " + k + "of node "+ j + "of layer " + i);
                    }
                    else{}
                   // System.out.print(nn.weights[i][j][k] + " ");

                }
                //System.out.println();
            }
        }
        //print the heavy hitters
        System.out.println("Our heavy hitters are:");
        for (int i = 0; i < noteables.size(); i++) {
            System.out.println(noteables.get(i));

        }
    }

    public  static  double test(NN nn, double[][]data, int []labels, int[] indexes, int print){

        nn.test = 1; //were testing now
        double[] testErrors = new double[indexes.length]; //array to store our testing errors

        for (int i = 0; i <indexes.length ; i++) {
            nn = feedForward(nn, data[indexes[i]], labels[indexes[i]]);
            testErrors[i] = nn.sampleError;
        }
        return MSE(testErrors);
        //if(print == 1){
            //jett you fill in this part
        //}
    }
    public static NN train(NN nn, double[][]data, int []labels, int[][] indexes, int validationSection){
        nn.test = 0;
        if(validationSection == 4){
            nn.epochErrors = new double[55*4];
        }
        else if(validationSection == -1){
            nn.epochErrors = new double[55*4 + 53];
        }
        else{
            nn.epochErrors = new double[55*3 +53];
        }
        int count;
        for(int i = 0; i < nn.numEpochs; i++){
            count = 0;
            for(int j = 0; j < indexes.length;j++){     //for each section of our cross validation
                if(j == validationSection){             //except our testing section
                    continue;
                }
                for (int k = 0; k < indexes[j].length ; k++) {
                    if(i == nn.numEpochs-1){
                        nn.test = 0;
                    }
                    //feedforward and  back propogate
                    nn = feedForward(nn, data[indexes[j][k]], labels[indexes[j][k]]);
                    nn.epochErrors[count] = nn.sampleError; // store output error
                    nn = backPropogate(nn);
                    count++;
                }


            }

            if(i%10 == 9 || i == 0 ||  i == nn.numEpochs-1) {
                System.out.print(i + ": ");
                System.out.println(MSE(nn.epochErrors));

            }
            /*
            if(MSE(nn.epochErrors) < .08){ //to prevent over training
                System.out.print(i + ": ");
                System.out.println(MSE(nn.epochErrors));
                break;
            }
            */
        }
        nn.printWeights();
        return nn;
    }
    public static NN feedForward(NN nn, double[] sample, int label){
        //add one to sample for constant
        double[] constant = new double[]{1};
        double [] newSample = new double[sample.length+1];
        System.arraycopy(constant, 0, newSample, 0, constant.length);
        System.arraycopy(sample, 0, newSample, 1, sample.length);

        //setting up the input layer
        nn.nodes[0] = newSample;

        //now to do the forward propogation
        for(int i = 1; i < nn.nodes.length-1; i++){ //for every layer (except the input/output layers)

            for (int j = 0; j < nn.weights[i-1][0].length; j++) { // for every weight (its fully connected so all nodes have the same amount of weights)
                double total = 0;
//                if(j+1>=nn.weights[i-1][j].length){
//                    continue;
//                }
                for (int k = 0; k < nn.nodes[i-1].length; k++){ //for every node of that layer (except the constant]


                    total += nn.weights[i-1][k][j] * nn.nodes[i-1][k]; //calc the total
                }

                nn.nodes[i][j+1] = (1 / (1 + Math.exp(-total))); //do sigmoid for that node


            }
        }
        //now for output layer
        double total = 0;
        for (int k = 0; k < nn.nodes[nn.nodes.length-2].length; k++) { // for every weight going to the output layer

            total += nn.nodes[nn.nodes.length-2][k]*nn.weights[nn.weights.length-1][k][0];

        }

        if (nn.test == 1){  //perceptron for when we test
            if((1 / (1 + Math.exp(-total)))>.5){
                nn.nodes[nn.nodes.length-1][0] =1;
            }
            else{
                nn.nodes[nn.nodes.length-1][0] = 0;
            }
        }
        else{           //else we are training and we want the sigmoid
            nn.nodes[nn.nodes.length-1][0] = (1 / (1 + Math.exp(-total)));
        }

        nn.sampleError = label - nn.nodes[nn.nodes.length-1][0];
        return nn;
    }
    public static NN backPropogate(NN nn){

        //first we shall deal with the weight change going to the output layer
        double deltaOutput = -nn.sampleError;


        //now for the hiddenl layers
        double[][] deltahidden = new double[nn.nodes.length-2][]; // the number of hidden layers = nn.nodes.length - output layer - input layer

        //allocating space and populating
        for (int i = 0; i < deltahidden.length; i++) {
            deltahidden[i] = new double[nn.nodes[i+1].length-1]; //the -1 gets rid of the bias
            for (int j = 0; j < deltahidden[i].length; j++) {
                deltahidden[i][j] = 0;
            }
        }

        for (int i = nn.nodes.length-2; i > 0; i--) { //starting at the last hidden layer and we don't want to do this for the input layer


            for (int k = 0; k < nn.nodes[i].length-1; k++){ //starting at the first non-bias node (we took it out at allocation


                for (int j = 0; j < nn.weights[i][k].length; j++) { //multiply the weights by the nodes of the layer above it
                    if(i == nn.nodes.length-2){
                        deltahidden[i-1][k] +=  nn.weights[i][k][j]*deltaOutput;
                    }
                    else{

                        deltahidden[i-1][k] +=  nn.weights[i][k][j]*deltaOutput; // deltahidden[i][j]; <-- the thing i took out
                    }

                }

                deltahidden[i-1][k] *= (nn.nodes[i][k+1]*(1-nn.nodes[i][k+1])); //  complete delta and store it nodevalue*(1-nodevalue)

            }
        }

        // then update the weights;
        //for every weight the update is w = w - a(the node its coming from)(the delta of the node its going to)
        for(int i = 0; i < nn.nodes.length-2; i++){ //for every layer (including the input layer and not including the output layer)

            for (int j = 0; j < nn.weights[i][0].length; j++) { // for every weight

                for (int k = 0; k < nn.nodes[i].length; k++){ //for every node of that layer (including the constant)
                    nn.weights[i][k][j] -= nn.learnRate*(nn.nodes[i][k] *deltahidden[i][j] ); //*(deltahidden[i][j] <- the thing I took out
                }

            }
        }
        //output layer
        for(int i= 0; i < nn.weights[nn.weights.length-1].length; i++){ //for every weight that connects to the output layer (the last layer of weights)
            nn.weights[nn.weights.length-1][i][0] -= nn.learnRate*(deltaOutput * nn.nodes[nn.nodes.length - 2][i]); //w= w- a(O-Y)x == w+a(Y-O)x
        }

        return nn;
    }
    public static double MSE(double[] errors){
        double sum = 0;
        for (int i = 0; i < errors.length; i++) {
            sum += Math.pow(errors[i], 2);
        }

        sum = sum/errors.length;
        return sum;
    }
    public static double[][] testData() {
        double[][] Data = new double[40][15361];
        String line;
        String sampleData;

        File ImageFolder = new File((System.getProperty("user.dir") + File.separator +"Test"));
        File[] Images = ImageFolder.listFiles();
        for(int i = 0; i < 40; i++) {
            for (File image : Images) {
                try {

                    FileReader fileReader = new FileReader(image);

                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    sampleData = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        //bufferedReader.read();
                        if (sampleData.equals("")) {
                            sampleData = line;
                        } else {
                            sampleData = sampleData + " " + line;  //convert file to string
                        }
                    }
                    //System.out.println(sampleData.split(" ").length); //Debugger
                    int count = 0;
                    for (String k : sampleData.split(" ")) { //for each feature
                        count++;
                        //cast to double and put into data for that sample, also div by 255 to scale the thing
                        Data[i][count] = Double.parseDouble(k) / 255;
                    }

                    bufferedReader.close();
                } catch (FileNotFoundException ex) {
                    System.out.println(
                            "Unable to open file '" +
                                    image.getName() + "'");
                } catch (IOException ex) {
                    System.out.println(
                            "Error reading file '"
                                    + image.getName() + "'");
                }
            }
        }
        return Data;
    }
    public static double[][] trainData(){
        double[][] Data = new double[273][15361];

        String line = null;
        String sampleData;

        String pathToMaleImages = System.getProperty("user.dir") + File.separator +"Male";
        File maleImageFolder = new File(pathToMaleImages);
        File[] maleImages = maleImageFolder.listFiles();
        int i = 0;
        if (maleImages != null) {

            for (File image : maleImages) {
                try {

                    FileReader fileReader = new FileReader(image);

                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    sampleData = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        //bufferedReader.read();
                        if (sampleData.equals("")){
                            sampleData = line;
                        }
                        else {
                            sampleData = sampleData + " " + line;  //convert file to string
                        }
                    }
                    //System.out.println(sampleData.split(" ").length); //Debugger
                    int count = 0;
                    for (String k : sampleData.split(" ")) { //for each feature
                        count++;
                        //cast to double and put into data for that sample, also div by 255 to scale the thing
                        Data[i][count] = Double.parseDouble(k)/255;
                    }

                    bufferedReader.close();
                } catch (FileNotFoundException ex) {
                    System.out.println(
                            "Unable to open file '" +
                                    image.getName() + "'");
                } catch (IOException ex) {
                    System.out.println(
                            "Error reading file '"
                                    + image.getName() + "'");
                }
                i++; // on to next sample

            }
        }
        else{
            System.out.println("images is null");
        }

        String pathToFemaleImages = System.getProperty("user.dir")+ File.separator +"Female";
        File femaleImageFolder = new File(pathToFemaleImages);
        File[] femaleImages = femaleImageFolder.listFiles();
        if (femaleImages != null) {
            for (File image : femaleImages) {
                try {

                    FileReader fileReader = new FileReader(image);

                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    sampleData = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        //bufferedReader.read();
                        if (sampleData.equals("")){
                            sampleData = line;
                        }
                        else {
                            sampleData = sampleData + " " + line;  //convert file to string
                        }
                    }
                    //System.out.println(sampleData.split(" ").length); //Debugger
                    int count = 0;
                    for (String k : sampleData.split(" ")) { //for each feature
                        count++;
                        //cast to double and put into data for that sample, also div by 255 to scale the thing
                        Data[i][count] = Double.parseDouble(k)/255;
                    }

                    bufferedReader.close();
                } catch (FileNotFoundException ex) {
                    System.out.println(
                            "Unable to open file '" +
                                    image.getName() + "'");
                } catch (IOException ex) {
                    System.out.println(
                            "Error reading file '"
                                    + image.getName() + "'");
                }
                i++; // on to next sample

            }
        }
        else{
            System.out.println("images is null");
        }
        return Data;
    }

    public static int[] randomizer(int[] array)
    {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }
}
