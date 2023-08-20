package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.google.common.collect.*;
import org.checkerframework.checker.nullness.qual.NonNull;


public class sortIt
{
    private static boolean isCorrectParameters = true;

    public static void main(String[] args)
    {
        // Not enough parameters handling
        if (args.length < 3)
        {
            System.out.println("Not enough arguments");
            System.out.println("Usage: java sort-it <sort_mode> <data_type> <output_file> <input_files...>");
            return;
        }

        String dataType = args[0]; // -i for integers, -s for strings
        // Wrong data type handling
        if (!dataType.equals("-i") && !dataType.equals("-s"))
        {
            System.out.printf("Incorrect data type parameter. Use -i for integers and -s for Strings.\n" +
                    "You used " + "%s\n", dataType);
            isCorrectParameters = false;
        }

        boolean isSortingModeSelected = (args[1].equals("-a") || args[1].equals("-d") || args[1].matches("-.+"));
        String sortMode = isSortingModeSelected ? args[1] : "-a"; /* -a for ascending sorting, -d for descending sorting.
                                                                                   Default - ascending mode */
        // Wrong sorting mode handling
        if (isSortingModeSelected && !sortMode.equals("-a") && !sortMode.equals("-d"))
        {
            System.out.printf("Incorrect sorting mode. Use -a for ascending and -d for descending.\n" +
                    "You used " + "%s\n", sortMode);
            isCorrectParameters = false;
        }

        String outputFileName = isSortingModeSelected ? args[2] : args[1];
        outputFileHandling(outputFileName);

        int inputFilesStartIndex = isSortingModeSelected ? 3 : 2;
        List<String> inputFiles = new ArrayList<>();
        //  Deleting duplicate input file names
        {
            HashSet<String> uniqueSetOfNames = new HashSet<>();
            uniqueSetOfNames.addAll(Arrays.asList(args).subList(inputFilesStartIndex, args.length));
            inputFiles.addAll(uniqueSetOfNames);
        }
        // Wrong input files handling
        inputFilesHandling(inputFiles);

        if (isCorrectParameters)
        {
            try
            {
                List<BufferedReader> readers = new ArrayList<>();
                for (String inputFile : inputFiles)
                {
                    readers.add(new BufferedReader(new FileReader(inputFile)));
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
                if (dataType.equals("-i"))
                {
                    mergeSortIntegers(readers, writer);
                }
                else if (dataType.equals("-s"))
                {
                    mergeSortStrings(readers, writer);
                }
                System.out.println("Program has been successfully completed.");
                System.out.println("Result is in: " + outputFileName);
            }
            // Input/Output problems handling
            catch (IOException exception)
            {
                System.out.println("An error occurred: " + exception.getMessage() + "\n" + "Cause is: " + exception.getCause());
            }
            // Discrepancy of data handling
            catch (NumberFormatException exception)
            {
                System.out.printf("""
                        Data doesn't match declaration
                        You declared data as %s, but real the data is %s
                        """, "integer", "String");
            }
        }
    }

    /**
     * Checks correctness of the input files' names and access on read
     *
     * @param inputFiles list of names of input files
     */
    private static void inputFilesHandling(@NonNull List<String> inputFiles)
    {
        @FunctionalInterface
        interface Messageble
        {
            void print(int index);
        }

        Messageble message = index ->
        {
            if (index == 0)
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + "%s" + " %s\n", inputFiles.get(index), inputFiles.get(index + 1));
                isCorrectParameters = false;
            }
            else if (index == inputFiles.size() - 1)
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + "%s " + "%s\n", inputFiles.get(index - 1), inputFiles.get(index));
                isCorrectParameters = false;
            }
            else
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + "%s " + "%s" + " %s\n", inputFiles.get(index - 1), inputFiles.get(index), inputFiles.get(index + 1));
                isCorrectParameters = false;
            }
        };

        for (int i = 0; i < inputFiles.size(); i++)
        {
            // Incorrect file name handling
            try
            {
                Path filePath = Paths.get(inputFiles.get(i));
                // File absence handling
                if (!Files.exists(filePath))
                {
                    System.out.println("No such file in this directory: " + inputFiles.get(i));
                    isCorrectParameters = false;
                    continue;
                }
                // Access violation handling
                if (!Files.isReadable(filePath))
                {
                    System.out.println("Can't read file: " + inputFiles.get(i));
                    isCorrectParameters = false;
                }
            }
            catch (InvalidPathException invalidPathException)
            {
                message.print(i);
                isCorrectParameters = false;
            }
            catch (SecurityException securityException)
            {
                System.out.println(securityException.getMessage());
                isCorrectParameters = false;
            }
        }
    }

    /**
     * Checks if it's possible to open output file or create it if there's no file with name "outputFileName"
     *
     * @param outputFileName
     */
    private static void outputFileHandling(@NonNull String outputFileName)
    {
        Path outputPath;
        // Incorrect file name handling
        try
        {
            outputPath = Paths.get(outputFileName);
        }
        catch (InvalidPathException invalidPathException)
        {
            System.out.printf("Incorrect output file name. Use file with correct name with .bin or .txt format.\n" +
                    "You used " + "%s\n", outputFileName);
            isCorrectParameters = false;
            return;
        }

        // Access violation handling
        try
        {
            boolean isWritable = Files.isWritable(outputPath);
            if (Files.exists(outputPath))
            {
                if (!isWritable)
                {
                    System.out.println("Impossible to open output file: " + outputFileName);
                    isCorrectParameters = false;
                }
            }
        }
        catch (SecurityException securityException)
        {
            System.out.println("Impossible to open output file: " + outputFileName);
            isCorrectParameters = false;
        }
    }

    /**
     * Sorts sequences of integers in files contained in "readers" and writes them into the "writer" output file
     *
     * @param readers     is a list of the input files from where we get data
     * @param writer      is the output file
     * @throws IOException           if it can't read data from files or write it into output file
     * @throws NumberFormatException if you set the data type as integer, but in files you have Strings
     */
    private static void mergeSortIntegers(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer)
            throws IOException, NumberFormatException
    {
        List<Integer> previousElements = new ArrayList<>(Collections.nCopies(readers.size(), Integer.MIN_VALUE)); // To check correctness of files sorting
        Multimap<Integer, Integer> sorter = TreeMultimap.create(); // Store numbers from files as a key and indexes of the files in "readers" as list of values
        // Reading first number from each file if it isn't empty
        for (int i = 0; i < readers.size(); i++)
        {
            if (readers.get(i).ready())
            {
                Integer currentFileElement = Integer.parseInt(readers.get(i).readLine().trim());
                sorter.put(currentFileElement, i);
            }
        }

        // Sorts integers by ascending order and writes them into output file
        while (!sorter.isEmpty())
        {
            int lowestElement = sorter.keys().iterator().next();
            int fileNum = sorter.get(lowestElement).iterator().next();
            BufferedReader currFile = readers.get(fileNum);

            writer.write(Integer.toString(lowestElement));
            writer.newLine();
            previousElements.set(fileNum, lowestElement);
            sorter.remove(lowestElement, fileNum);

            // If all data hadn't been read from file with the lowest element at the moment, reading another element
            if (currFile.ready())
            {
                int newValueFromCurrFile = Integer.parseInt(currFile.readLine().trim());
                // Checking that we got correct data. If not, taking new element until we get correct element or until file ends
                while (newValueFromCurrFile < lowestElement)
                {
                    if (currFile.ready())
                    {
                        newValueFromCurrFile = Integer.parseInt(currFile.readLine().trim());
                    }
                    else
                    {
                        break;
                    }
                }

                if (newValueFromCurrFile >= lowestElement)
                {
                    sorter.put(newValueFromCurrFile, fileNum);
                }
            }
        }
        for (BufferedReader reader : readers)
        {
            reader.close();
        }
        writer.flush();
        writer.close();
    }

    /**
     * Sorts sequences of Strings in files contained in "readers" and writes them into the "writer" output file
     *
     * @param readers     is a list of the input files from where we get data
     * @param writer      is the output file
     * @throws IOException if it can't read data from files or write it into output file
     */
    private static void mergeSortStrings(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer)
            throws IOException
    {
        List<String> previousElements = new ArrayList<>(Collections.nCopies(readers.size(), "")); // To check correctness of files sorting
        Multimap<String, Integer> sorter = TreeMultimap.create(); // Store numbers from files as a key and indexes of the files in "readers" as list of values
        // Reading first number from each file if it isn't empty
        for (int i = 0; i < readers.size(); i++)
        {
            if (readers.get(i).ready())
            {
                String currentFileElement = readers.get(i).readLine().trim();
                sorter.put(currentFileElement, i);
            }
        }

        // Sorts integers by ascending order and writes them into output file
        while (!sorter.isEmpty())
        {
            String lowestElement = sorter.keys().iterator().next();
            int fileNum = sorter.get(lowestElement).iterator().next();
            BufferedReader currFile = readers.get(fileNum);

            writer.write(lowestElement);
            writer.newLine();
            previousElements.set(fileNum, lowestElement);
            sorter.remove(lowestElement, fileNum);

            // If all data hadn't been read from file with the lowest element at the moment, reading another element
            if (currFile.ready())
            {
                String newValueFromCurrFile = currFile.readLine().trim();
                // Checking that we got correct data. If not, taking new element until we get correct element or until file ends
                while (newValueFromCurrFile.compareTo(lowestElement) < 0)
                {
                    if (currFile.ready())
                    {
                        newValueFromCurrFile = currFile.readLine().trim();
                    }
                    else
                    {
                        break;
                    }
                }

                if (newValueFromCurrFile.compareTo(lowestElement) >= 0)
                {
                    sorter.put(newValueFromCurrFile, fileNum);
                }
            }
        }
        for (BufferedReader reader : readers)
        {
            reader.close();
        }
        writer.flush();
        writer.close();
    }
}

