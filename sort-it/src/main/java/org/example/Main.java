package org.example;

import java.io.*;
import java.util.*;

import com.google.common.collect.*;
import org.checkerframework.checker.nullness.qual.NonNull;


public class Main
{
    public static void main(String[] args)
    {

        /*Multimap<Integer, Integer> example = HashMultimap.create();
        for (int i = 100; i > 0; i--)
        {
            for (int j = 1000; j > 0; j--)
            {
                example.put(i, j);
                example.put(j, i);
            }
        }
        System.out.println(example.keys().iterator().next());
        example.remove(example.keys().iterator().next(), example.get(example.keys().iterator().next()).iterator().next());
        System.out.println(example.keys().iterator().next());*/

        if (args.length < 3)
        {
            System.out.println("Usage: java sort-it <sort_mode> <data_type> <output_file> <input_files...>");
            return;
        }

        String dataType = args[0]; // -i для целых чисел, -s для строк
        String sortMode = (args[1].equals("-a") || args[1].equals("-d")) ? args[1] : "-a"; /* -a для сортировки по возрастанию, -d для сортировки по убыванию.
                                                                                   По умолчанию стоит режим сортировки по возрастанию */
        String outputFile = (args[1].equals("-a") || args[1].equals("-d")) ? args[2] : args[1];

        int inputFilesStartIndex = (args[1].equals("-a") || args[1].equals("-d")) ? 3 : 2;

        List<String> inputFiles = new ArrayList<>();
        for (int i = inputFilesStartIndex; i < args.length; i++)
        {
            inputFiles.add(args[i]);
        }

        try
        {
            List<DataInputStream> readers = new ArrayList<>();
            for (String inputFile : inputFiles)
            {
                readers.add(new DataInputStream(new FileInputStream(inputFile)));
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            if (dataType.equals("-i"))
            {
                mergeSortIntegers(readers, writer, sortMode.equals("-a"));
            }
            else if (dataType.equals("-s"))
            {
                mergeSortStrings(readers, writer, sortMode.equals("-a"));
            }

        }
        catch (IOException exception)
        {
            System.out.println("An error occurred: " + exception.getMessage());
        }
    }

    private static void mergeSortIntegers(@NonNull List<DataInputStream> readers, @NonNull BufferedWriter writer, boolean isAscending)
            throws IOException
    {
        Comparator<Integer> comparator = isAscending ? // Создаем компаратор в зависимости от выбранного режима сортировки
                (left, right)->left.compareTo(right):
                (left, right)->right.compareTo(left);

        List<Integer> previousElements = new ArrayList<>(Collections.nCopies(readers.size(), Integer.MIN_VALUE));
        Multimap<Integer, Integer> sorter = HashMultimap.create(readers.size(), readers.size());
        for (int i = 0; i < readers.size(); i++)
        {
            Integer currentFileElement = readers.get(i).readInt();
            sorter.put(currentFileElement, i);
        }

        // Числа сортируем по убыванию и записываем их в выходной файл
        while (!sorter.isEmpty())
        {
            int lowestElement = sorter.keys().iterator().next();
            int fileNum = sorter.get(lowestElement).iterator().next();
            DataInputStream currFile = readers.get(fileNum);

            writer.write(lowestElement);
            writer.newLine();
            previousElements.set(fileNum, lowestElement);
            sorter.remove(lowestElement, fileNum);

            if(currFile != null)
            {
                int newValueFromCurrFile = currFile.readInt();
                boolean isLastElement = (currFile.available() <= 0);

                if (!isLastElement)
                {
                    while (newValueFromCurrFile < lowestElement)
                    {
                        if (currFile.available() > 0)
                        {newValueFromCurrFile = currFile.readInt();}

                        else
                        {
                            isLastElement = true;
                            currFile.close();
                            currFile = null;
                            break;
                        }
                    }
                }
                else
                {
                    currFile.close();
                    currFile = null;
                    continue;
                }

                if (newValueFromCurrFile < lowestElement)
                {sorter.put(newValueFromCurrFile, fileNum);}
            }
        }
        writer.close();
    }

    private static void mergeSortStrings(@NonNull List<DataInputStream> readers, @NonNull BufferedWriter writer, boolean isAscending)
            throws IOException
    {

    }
}

