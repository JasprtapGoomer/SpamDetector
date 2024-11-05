package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.math.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */


public class SpamDetector {

    // Initialize accuracy and precision variables
    double accuracy;
    double precision;

    // Getters
    public double getAccuracy(){return this.accuracy;}

    public double getPrecision() {return this.precision;}

    // Function to get spam and ham frequency maps (Hashmap with words as keys and the frequency as values)

    public Map<String, Integer> getFrequencyMap(File dirPath){

        Map<String, Integer> FreqMap = new HashMap<>();

        // Iterate over all children in directory

        File[] directoryListing = dirPath.listFiles();
        //System.out.println("Directory Listing Created!");
        //System.out.println(dirPath.getPath());
        assert directoryListing != null;
        for (File child : directoryListing) {
            // Generate bag of words for file
            Set<String> words = generateBagOfWords(child.getPath());

            // Update frequency map by adding or updating word counts
            for (String word: words){

                if (!FreqMap.containsKey(word)){

                    FreqMap.put(word, 1);

                }
                else {

                    Integer integer = FreqMap.get(word);

                    FreqMap.put(word, integer + 1);

                }

            }
        }


    return FreqMap;
    }


    // Function to get word spam probabilities using frequency maps
    public Map <String, Double> getSpamProbabilities(Map<String, Integer> spamFreq, Map<String, Integer> hamFreq, File spamDir, File hamDir){


        // Create directory listings
        File[] spamDirectoryListing = spamDir.listFiles();
        File[] hamDirectoryListing = hamDir.listFiles();


        // Get the amount of total files
        double spamFiles = spamDirectoryListing.length;
        double hamFiles = hamDirectoryListing.length;


        // Create tree maps to hold spam probabilities
        Map<String, Double> wordGivenSpam = new TreeMap<>();
        Map<String, Double> wordGivenHam = new TreeMap<>();


        // Iterate over frequency maps and calculate word spam/ham probabilities
        for (String word: spamFreq.keySet()){

            Integer freq = spamFreq.get(word);

            Double spamProb = freq / spamFiles;

            wordGivenSpam.put(word, spamProb);

        }

        for (String word: hamFreq.keySet()){

            Integer freq = hamFreq.get(word);

            Double hamProb = freq / hamFiles;

            wordGivenHam.put(word, hamProb);

        }

        // Calculate spam given word map based on Naive Bayes spam filtering

        Map<String, Double> spamGivenWordMap = new TreeMap<>();

        for (String word: wordGivenSpam.keySet()){

            Double spamProbabilityOfWord = wordGivenSpam.get(word);
            Double hamProbabilityOfWord = wordGivenHam.getOrDefault(word, 0.0);

            Double spamGivenWord = spamProbabilityOfWord / (hamProbabilityOfWord + spamProbabilityOfWord);

            spamGivenWordMap.put(word, spamGivenWord);


        }

        return spamGivenWordMap;

    }


    // Generate bag of words for a file

    public Set<String> generateBagOfWords(String filePath) {
        Set<String> bagOfWords = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into words
                String[] words = line.split("\\s+");

                for (String word : words) {
                    // Convert the word to lowercase to make the model case-insensitive
                    word = word.toLowerCase();

                    // Remove non-alphabetic characters
                    word = word.replaceAll("[^a-zA-Z]", "");

                    bagOfWords.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bagOfWords;
    }

    // Function to make a TestFile for a given file
    public TestFile checkFile(File filePath, String fileClass, Map<String,Double> spamProbabilities) {
        // Set eta parameter
        double eta = 0.0;


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into words
                String[] words = line.split("\\s+");

                for (String word : words) {
                    // Convert the word to lowercase to make the model case-insensitive
                    word = word.toLowerCase();

                    // Remove non-alphabetic characters
                    word = word.replaceAll("[^a-zA-Z]", "");
                    
                    
                    // Calculate eta term

                    Double wordSpamProbability = spamProbabilities.get(word);
                    if (wordSpamProbability == null || wordSpamProbability == 1)continue;

                    double summationTerm = Math.log(1 - wordSpamProbability) - Math.log(wordSpamProbability);

                    eta += summationTerm;


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        // Calculate file spam probability
        double fileSpamProbability = 1 / (1 + Math.exp(eta));
        //System.out.println(fileSpamProbability);
        // Create TestFile
        TestFile testfile = new TestFile(filePath.getName(), fileSpamProbability, fileClass);
        return testfile;
    }




    public List<TestFile> trainAndTest(File mainDirectory) {
//        TODO: main method of loading the directories and files, training and testing the model


        // Initialize variables

        String mainPath = mainDirectory.getPath();
        String testSpamPath = mainPath + "/test/spam";
        String trainSpamPath = mainPath + "/train/spam";
        String testHamPath = mainPath + "/test/ham";
        String trainHamPath = mainPath + "/train/ham";
        File testSpamDir = new File(testSpamPath);
        File testHamDir = new File(testHamPath);
        File trainHamDir = new File(trainHamPath);
        File trainSpamDir = new File(trainSpamPath);

        
        // Create frequency and spam maps
        Map<String, Integer> trainSpamFreq = getFrequencyMap(trainSpamDir);
        Map<String, Integer> trainHamFreq = getFrequencyMap(trainHamDir);
        Map<String, Double> spamGivenWordMap = getSpamProbabilities(trainSpamFreq, trainHamFreq, trainSpamDir, trainHamDir);

        File[] spamDirectoryListing = testSpamDir.listFiles();
        File[] hamDirectoryListing = testHamDir.listFiles();

        List<TestFile> testFiles = new ArrayList<TestFile>();

        //int counter = 0;


        // Create TestFiles for spam and ham
        assert spamDirectoryListing != null;
        for (File child: spamDirectoryListing){

            //System.out.println(counter);
            TestFile spamFile = checkFile(child, "Spam", spamGivenWordMap);
            testFiles.add(spamFile);
            //counter++;
        }

        assert hamDirectoryListing != null;
        for (File child: hamDirectoryListing){
            //System.out.println(counter);
            TestFile hamFile = checkFile(child, "Ham", spamGivenWordMap);
            testFiles.add(hamFile);
            //counter++;

        }

        // Calculate accuracy and precision
        int totalFiles = hamDirectoryListing.length + spamDirectoryListing.length;
        double truePositives = 0;
        double trueNegatives = 0;
        double falsePositives = 0;

        for (int i = 0; i < totalFiles; i++){

            TestFile currentFile = testFiles.get(i);


            if(currentFile.getSpamProbability() >= 0.5 && currentFile.getActualClass().equals("Spam")) truePositives++;
            else if(currentFile.getSpamProbability() >= 0.5 && currentFile.getActualClass().equals("Ham")) falsePositives++;
            else if (currentFile.getSpamProbability() < 0.5 && currentFile.getActualClass().equals("Ham")) trueNegatives++;

        }

        this.accuracy = (trueNegatives + truePositives) / totalFiles;
        this.precision = truePositives / (falsePositives+ truePositives);




        return testFiles;
    }

}
