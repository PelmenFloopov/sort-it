package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.google.common.collect.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sound.midi.Soundbank;


public class sortIt
{
    // Formatted output constants
    private static final String UNDERLINE = "\u001B[4m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private static boolean isCorrectParameters = true;

    public static void main(String[] args)
    {
        /*for (int i = 1; i < 4; i++)
        {
            try
            {
                File tmp = new File("in" + Integer.toString(i) + ".txt");
                if (!tmp.exists())
                {
                    tmp.createNewFile();
                }
                else
                {
                    tmp.delete();
                    tmp.createNewFile();
                }
                BufferedWriter w = new BufferedWriter(new FileWriter(tmp));
                for (int j = 1; j <= 10_000_000; j++)
                {
                    w.write(Integer.toString(j * (i % 3)));
                    w.newLine();
                }
                w.close();
            }
            catch (IOException exception)
            {
                System.out.println(exception.getMessage());
            }
        }*/

        long start = System.currentTimeMillis();

        // Not enough parameters handling
        if (args.length < 3)
        {
            System.out.println("Usage: java sort-it <sort_mode> <data_type> <output_file> <input_files...>");
            return;
        }

        String dataType = args[0]; // -i for integers, -s for strings
        // Wrong data type handling
        if (!dataType.equals("-i") && !dataType.equals("-s"))
        {
            System.out.printf("Wrong data type parameter. Use -i for integers and -s for Strings.\n" +
                    "You used " + RED + "%s\n" + RESET, dataType);
            isCorrectParameters = false;
        }

        boolean isSortingModeSelected = (args[1].equals("-a") || args[1].equals("-d") || args[1].matches("-.+"));
        String sortMode = isSortingModeSelected ? args[1] : "-a"; /* -a for ascending sorting, -d for descending sorting.
                                                                                   Default - ascending mode */
        // Wrong sorting mode handling
        if (isSortingModeSelected && !sortMode.equals("-a") && !sortMode.equals("-d"))
        {
            System.out.printf("Wrong sorting mode. Use -a for ascending and -d for descending.\n" +
                    "You used " + RED + "%s\n" + RESET, sortMode);
            isCorrectParameters = false;
        }

        String outputFileName = isSortingModeSelected ? args[2] : args[1];


        int inputFilesStartIndex = isSortingModeSelected ? 3 : 2;
        List<String> inputFiles = new ArrayList<>();
        inputFiles.addAll(Arrays.asList(args).subList(inputFilesStartIndex, args.length));

        // Wrong input file handling
        inputFileNamesHandling(inputFiles);

        if (isCorrectParameters)
        {
            try
            {
                List<BufferedReader> readers = new ArrayList<>();
                for (String inputFile : inputFiles)
                {
                    readers.add(new BufferedReader(new FileReader(inputFile)));
                }

                // Wrong output file name handling
                try
                {
                    Path outputPath = Paths.get(outputFileName);
                    // Closed output file handling
                    try
                    {
                        boolean isWritable = Files.isWritable(outputPath);
                        if (!isWritable)
                        {
                            System.out.println("Impossible to open output file. Output file is closed");
                        }
                    }
                    catch (SecurityException securityException)
                    {
                        System.out.println("Impossible to open output file. Output file is closed");
                    }
                }
                catch (InvalidPathException invalidPathException)
                {
                    System.out.printf("Wrong output file name. Use file with correct name with .bin or .txt format.\n" +
                            "You used " + RED + "%s\n" + RESET, outputFileName);
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

                if (dataType.equals("-i"))
                {
                    mergeSortIntegers(readers, writer, sortMode.equals("-a"));
                }
                else if (dataType.equals("-s"))
                {
                    mergeSortStrings(readers, writer, sortMode.equals("-a"));
                }
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
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }

    /**
     * Checks correctness of the input files' names
     *
     * @param inputFiles
     */
    private static void inputFileNamesHandling(@NonNull List<String> inputFiles)
    {
        @FunctionalInterface
        interface Messageble
        {
            void print(int index);
        }

        Messageble message =  index->
        {
            if (index == 0)
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + (RED + UNDERLINE) + "%s" + RESET + " %s\n", inputFiles.get(index), inputFiles.get(index + 1));
                isCorrectParameters = false;
            }
            else if (index == inputFiles.size() - 1)
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + "%s " + (RED + UNDERLINE) + "%s\n" + RESET, inputFiles.get(index - 1), inputFiles.get(index));
                isCorrectParameters = false;
            }
            else
            {
                System.out.printf("Wrong input file name. Use file with correct name with .bin or .txt format.\n" +
                        "You used " + "%s " + (RED + UNDERLINE) + "%s" + RESET + " %s\n", inputFiles.get(index - 1), inputFiles.get(index), inputFiles.get(index + 1));
                isCorrectParameters = false;
            }
        };

        for (int i = 0; i < inputFiles.size(); i++)
        {
            try
            {
                Path filePath = Paths.get(inputFiles.get(i));
                if (!Files.exists(filePath))
                {
                    System.out.println("No such file in this directory");
                }
                if (!Files.isReadable(filePath))
                {

                }

            }
            catch (InvalidPathException invalidPathException)
            {
                message.print(i);
            }
            catch (SecurityException securityException)
            {
                message.print(i);
            }
        }
    }

    /**
     * Sorts sequences of integers in files contained in "readers" and writes them into the "writer" output file
     *
     * @param readers     is a list of the input files from where we get data
     * @param writer      is the output file
     * @param isAscending is the sorting mode
     * @throws IOException           if it can't read data from files or write it into output file
     * @throws NumberFormatException if you set the data type as integer, but in files you have Strings
     */
    private static void mergeSortIntegers(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer, boolean isAscending)
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
     * @param isAscending is the sorting mode
     * @throws IOException if it can't read data from files or write it into output file
     */
    private static void mergeSortStrings(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer, boolean isAscending)
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

