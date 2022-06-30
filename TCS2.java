import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Program that converts a text glossary into an alphabetized HTML glossary with
 * a directory and a separate page for each term.
 *
 * @author Ben Elleman
 *
 */
public final class TCS2 {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TCS2() {
    }

    /**
     * Minimum font size.
     */
    static final int FONT_MIN = 11;

    /**
     * Maximum font size.
     */
    static final int FONT_MAX = 48;

    /**
     * Compare {@code int}s in numerical order.
     */
    private static class IntAT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> a,
                Map.Entry<String, Integer> b) {
            return b.getValue() - a.getValue();
        }
    }

    /**
     * Compare {@code pair}s in alphabetical order.
     */
    private static class StringAT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int x = o1.getKey().compareTo(o2.getKey());
            if (x == 0) {
                x = o1.getValue().compareTo(o2.getValue());
            }
            return x;
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            The command line arguments; unused here
     */
    public static void main(String[] args) {
        try {
            //Open I/O streams.
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in));
            //Input file and output file.
            System.out.print("Enter a valid text file: ");
            String inputFileName;
            inputFileName = in.readLine();
            BufferedReader inFile = new BufferedReader(
                    new FileReader(inputFileName));
            System.out.print("Enter an output file name: ");
            String outputFile = in.readLine();
            System.out.print("Enter how many words you want to be included: ");
            String next = in.readLine();
            int cloudSize;
            cloudSize = Integer.parseInt(next);
            PrintWriter output = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));
            Map<String, Integer> reducedMap = mapGenerator(inFile);
            mapReducer(reducedMap, cloudSize);
            List<Entry<String, Integer>> sorted = alphabetize(reducedMap);
            htmlHeader(output, inputFileName, cloudSize);
            htmlBody(output, sorted);
            htmlFooter(output);
            //Closing IO
            in.close();
            output.close();
            inFile.close();
            //Confirmation message
            System.out.println("File successfully generated");
        } catch (NumberFormatException e) {
            System.out.println("You must provide a valid number.");
        } catch (IOException e1) {
            System.out.println("Not a valid input/output.");
            e1.printStackTrace();
        } catch (IndexOutOfBoundsException e2) {
            System.out.println("Map is out of bound");
        }
    }

    /**
     * Method to generate a map of the cloudSizeth most common tags and the
     * amount of times they occur.
     *
     * @param inFile
     *            The file input stream
     * @return A map of the most common cloudSize elements and their occurrences
     */
    private static Map<String, Integer> mapGenerator(BufferedReader inFile) {
        //List to keep count of the occurrences of the cloudSizeth most items
        Map<String, Integer> tags = new HashMap<>();
        String line = "!";
        try {
            while (line != null) {
                line = line.concat("!");
                final String separators = "! ,\t\n\r-.?[]';:/()";
                String nextTag = "";
                do {
                    char c = line.charAt(0);
                    if (separators.indexOf(c) == -1) {
                        nextTag += c;
                    } else if (nextTag.length() > 0) {
                        nextTag = nextTag.toLowerCase();
                        if (!tags.containsKey(nextTag)) {
                            tags.put(nextTag, 1);
                        } else {
                            int value = tags.remove(nextTag);
                            tags.put(nextTag, value + 1);
                        }
                        nextTag = "";
                    }
                    line = line.substring(1);
                } while (line.length() > 0);
                line = inFile.readLine();
            }
        } catch (IOException e) {
            System.out.println("An I/O error has occured");
        }
        return tags;
    }

    /**
     * Method to reduce the given map to only the the cloudSizeth most common
     * tags and the amount of times they occur.
     *
     * @param reducedMap
     *            Map of the cloudSizeth most common tags and the amount of
     *            times they occur.
     * @param cloudSize
     *            The amount of items to return
     */
    private static void mapReducer(Map<String, Integer> reducedMap,
            int cloudSize) {
        List<Entry<String, Integer>> decrease = new LinkedList<>();
        decrease.addAll(reducedMap.entrySet());
        Collections.sort(decrease, new IntAT());
        reducedMap.clear();
        int minCount = 0;
        int maxCount = 0;
        while (reducedMap.size() < cloudSize && decrease.size() > 0) {
            Entry<String, Integer> p = decrease.remove(0);
            reducedMap.put(p.getKey(), p.getValue());
            if (minCount > p.getValue() || minCount == 0) {
                minCount = p.getValue();
            }
            if (maxCount < p.getValue() || minCount == 0) {
                maxCount = p.getValue();
            }
        }
    }

    /**
     * Method to alphabetize the given Map of tags.
     *
     * @param reducedMap
     *            Tags to be alphabetized
     * @return A list of alphabetized tags
     */
    private static List<Entry<String, Integer>> alphabetize(
            Map<String, Integer> reducedMap) {
        List<Entry<String, Integer>> alpha = new LinkedList<>();
        alpha.addAll(reducedMap.entrySet());
        Collections.sort(alpha, new StringAT());
        return alpha;
    }

    /**
     * Output the headers for the HTML file.
     *
     *
     * @param output
     *            The HTML document to write on
     * @param filename
     *            Name of the inputed file
     * @param cloudSize
     *            Size of file
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    public static void htmlHeader(PrintWriter output, String filename,
            int cloudSize) {
        output.println("<html>");
        output.println("<head>");
        output.println("<title>" + filename + "</title>");
        output.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/"
                        + "web-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        output.println("<link href=\"tagcloud.css\" "
                + "rel=\"stylesheet\" type=\"text/css\">");
        output.println("</head>");
        output.println("<body><body style=\"background-color:#70839e;\">");
        output.println(
                "<h2>Top " + cloudSize + " words in " + filename + "</h2>");
        output.println("<hr>");
        output.println("<div class=\"cdiv\">");
        output.println("<p class=\"cbox\">");
    }

    /**
     * Output the body of the HTML file.
     *
     * @param output
     *            The HTML document to write on
     * @param sorted
     *            Alphabetized tags
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    public static void htmlBody(PrintWriter output,
            List<Entry<String, Integer>> sorted) {
        int[] minMax = minAndMax(sorted);
        int minCount = minMax[0];
        int maxCount = minMax[1];
        while (sorted.size() > 0) {
            Entry<String, Integer> pair = sorted.remove(0);
            int count = pair.getValue();
            while (maxCount <= minCount) {
                maxCount++;
            }
            int font = 0;
            if (count > minCount) {
                font = (int) (Math
                        .ceil((double) (FONT_MAX - 11) * (count - minCount))
                        / (maxCount - minCount));
            }
            //Floor
            font += 11;
            output.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\" title=\"count: " + pair.getValue() + "\">"
                    + pair.getKey() + "</span>");
        }
    }

    /**
     * Finds the min and max of a List.
     *
     * @param sorted
     *            Alphabetized tags
     * @return A pair of the min and max
     */
    private static int[] minAndMax(List<Entry<String, Integer>> sorted) {
        int min = -1;
        int max = -1;
        List<Entry<String, Integer>> temp = new LinkedList<>();
        while (sorted.size() > 0) {
            Entry<String, Integer> pair = sorted.remove(0);
            if (min > pair.getValue() || min == -1) {
                min = pair.getValue();
            }
            if (max < pair.getValue() || max == -1) {
                max = pair.getValue();
            }
            temp.add(pair);
        }
        sorted.addAll(temp);
        int[] minMax = new int[] { min, max };
        //While this could return -1 for either, this does not matter
        return minMax;
    }

    /**
     * Output the footers for the HTML files.
     *
     * @param output
     *            The HTML document to write on
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    public static void htmlFooter(PrintWriter output) {
        output.println("</p>");
        output.println("</div>");
        output.println("</body>");
        output.println("</html>");
    }
}
