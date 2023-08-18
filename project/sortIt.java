package org.example;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;
import org.checkerframework.checker.nullness.qual.NonNull;



public class sortIt
{
    // Formatted output constants
    private static final String UNDERLINE = "\u001B[4m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private static boolean isCorrectParameters = true;

    public static void main(String[] args)
    {
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
            System.out.printf("Wrong sorting mode. Use -a for ascending and -d for descending.\n"+
                    "You used " + RED + "%s\n" + RESET, sortMode);
            isCorrectParameters = false;
        }

        String outputFileName = isSortingModeSelected ? args[2] : args[1];
        // Wrong output file name handling
        if (!outputFileName.matches("^[^\\\\/?*:;{}\\[\\]<>|\"']+\\.txt$"))
        {
            System.out.printf("Wrong output file name. Use file with correct name with format .txt.\n" +
                    "You used " + RED + "%s\n" + RESET, outputFileName);
            isCorrectParameters = false;
        }

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
                        """, dataType.equals("-i") ? "integer":"String", dataType.equals("-i") ? "String": "integer");
            }
        }
    }

    private static void inputFileNamesHandling(@NonNull List<String> inputFiles)
    {
        for (int i = 0; i < inputFiles.size(); i++)
        {
            if (!inputFiles.get(i).matches("^[^\\\\/?*:;{}\\[\\]<>|\"']+\\.txt$"))
            {
                if (i == 0)
                {
                    System.out.printf("Wrong input file name. Use file with correct name with format .txt.\n" +
                            "You used " + (RED + UNDERLINE) + "%s" + RESET + " %s\n", inputFiles.get(i), inputFiles.get(i + 1));
                    isCorrectParameters = false;
                }
                else if (i == inputFiles.size() - 1)
                {
                    System.out.printf("Wrong input file name. Use file with correct name with format .txt.\n" +
                            "You used " + "%s " + (RED + UNDERLINE) + "%s\n" + RESET, inputFiles.get(i - 1), inputFiles.get(i));
                    isCorrectParameters = false;
                }
                else
                {
                    System.out.printf("Wrong input file name. Use file with correct name with format .txt.\n" +
                            "You used " + "%s " + (RED + UNDERLINE) + "%s" + RESET + " %s\n", inputFiles.get(i-1), inputFiles.get(i), inputFiles.get(i + 1));
                    isCorrectParameters = false;
                }
            }
        }
    }

    /**
     * Sorts sequences of integers in files contained in "readers" and writes them into the "writer" output file
     *
     * @param readers     is a list of the input files from where we get integers
     * @param writer      is the output file
     * @param isAscending is the sorting mode
     * @throws IOException
     */
    private static void mergeSortIntegers(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer, boolean isAscending)
            throws IOException, NumberFormatException
    {
        List<Integer> previousElements = new ArrayList<>(Collections.nCopies(readers.size(), Integer.MIN_VALUE)); // To check correctness of files sorting
        Multimap<Integer, Integer> sorter = TreeMultimap.create();
        for (int i = 0; i < readers.size(); i++)
        {
            Integer currentFileElement = Integer.parseInt(readers.get(i).readLine().trim());
            sorter.put(currentFileElement, i);
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

            if (currFile.ready())
            {
                int newValueFromCurrFile = Integer.parseInt(currFile.readLine().trim());
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

    private static void mergeSortStrings(@NonNull List<BufferedReader> readers, @NonNull BufferedWriter writer, boolean isAscending)
            throws IOException, NumberFormatException
    {
        List<String> previousElements = new ArrayList<>(Collections.nCopies(readers.size(), "")); // To check correctness of files sorting
        Multimap<String, Integer> sorter = TreeMultimap.create();
        for (int i = 0; i < readers.size(); i++)
        {
            String currentFileElement = readers.get(i).readLine().trim();
            sorter.put(currentFileElement, i);
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

            if (currFile.ready())
            {
                String newValueFromCurrFile = currFile.readLine().trim();
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

