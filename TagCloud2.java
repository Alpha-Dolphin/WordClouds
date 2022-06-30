import java.util.Comparator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * Program that converts a text glossary into an alphabetized HTML glossary with
 * a directory and a separate page for each term.
 *
 * @author Ben Elleman
 *
 */
public final class TagCloud2 {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloud2() {
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
     * Compare {@code pair}s in alphabetical order.
     */
    private static class StringAT
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            int x = o1.key().compareTo(o2.key());
            if (x == 0) {
                x = o1.value().compareTo(o2.value());
            }
            return x;
        }
    }

    /**
     * Compare {@code int}s in numerical order.
     */
    private static class IntAT
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> a,
                Map.Pair<String, Integer> b) {
            return b.value() - a.value();
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            The command line arguments; unused here
     */
    public static void main(String[] args) {
        //Open I/O streams.
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        //Input file and output file.
        out.print("Enter a valid text file: ");
        String inputFileName = in.nextLine();
        SimpleReader inFile = new SimpleReader1L(inputFileName);
        out.print("Enter an output file name: ");
        String outputFile = in.nextLine();
        out.print("Enter how many words you want to be included: ");
        String next = in.nextLine();
        int cloudSize = Integer.parseInt(next);
        SimpleWriter output = new SimpleWriter1L(outputFile);
        Map<String, Integer> reducedMap = mapGenerator(inFile);
        mapReducer(reducedMap, cloudSize);
        SortingMachine<Map.Pair<String, Integer>> sorted = alphabetize(
                reducedMap);
        htmlHeader(output, inputFileName);
        htmlBody(output, sorted);
        htmlFooter(output);
        //Confirmation message
        out.println("File successfully generated");
        //Closing IO
        in.close();
        out.close();
        output.close();
        inFile.close();
    }

    /**
     * Method to generate a map of the cloudSizeth most common tags and the
     * amount of times they occur.
     *
     * @param inFile
     *            The file input stream
     * @return the map of the most common cloudSize elements and their
     *         occurrences
     */
    private static Map<String, Integer> mapGenerator(SimpleReader inFile) {
        //List to keep count of the occurrences of the cloudSizeth most items
        Map<String, Integer> tags = new Map1L<>();
        while (!inFile.atEOS()) {
            String line = inFile.nextLine() + '?';
            final String separators = "! ,\t\n\r-.?[]';:/()";
            String nextTag = "";
            do {
                char c = line.charAt(0);
                if (separators.indexOf(c) == -1) {
                    nextTag += c;
                } else if (nextTag.length() > 0) {
                    nextTag = nextTag.toLowerCase();
                    if (!tags.hasKey(nextTag)) {
                        tags.add(nextTag, 1);
                    } else {
                        int value = tags.value(nextTag);
                        tags.replaceValue(nextTag, value + 1);
                    }
                    nextTag = "";
                }
                line = line.substring(1);
            } while (line.length() > 0);
        }
        return tags;
    }

    /**
     * Method to reduce the given map to only the the cloudSizeth most common
     * tags and the amount of times they occur.
     *
     * @param tags
     *            Map of the cloudSizeth most common tags and the amount of
     *            times they occur.
     * @param cloudSize
     *            The amount of items to return
     */
    private static void mapReducer(Map<String, Integer> tags, int cloudSize) {
        Comparator<Map.Pair<String, Integer>> ints = new IntAT();
        SortingMachine<Map.Pair<String, Integer>> decreasing = new SortingMachine1L<>(
                ints);
        while (tags.size() > 0) {
            Map.Pair<String, Integer> pair = tags.removeAny();
            decreasing.add(pair);
        }
        decreasing.changeToExtractionMode();
        tags.clear();
        int minCount = 0;
        int maxCount = 0;
        while (tags.size() < cloudSize && decreasing.size() > 0) {
            Pair<String, Integer> p = decreasing.removeFirst();
            tags.add(p.key(), p.value());
            if (minCount > p.value() || minCount == 0) {
                minCount = p.value();
            }
            if (maxCount < p.value() || minCount == 0) {
                maxCount = p.value();
            }
        }
    }

    /**
     * Method to alphabetize the given Map of tags.
     *
     * @param tags
     *            tags to be alphabetized
     * @return a SortingMachine of alphabetized tags
     */
    private static SortingMachine<Map.Pair<String, Integer>> alphabetize(
            Map<String, Integer> tags) {
        Comparator<Map.Pair<String, Integer>> tag = new StringAT();

        SortingMachine<Map.Pair<String, Integer>> alpha = new SortingMachine1L<>(
                tag);

        while (tags.size() > 0) {
            Map.Pair<String, Integer> pair = tags.removeAny();
            alpha.add(pair);
        }
        alpha.changeToExtractionMode();

        return alpha;
    }

    /**
     * Output the headers for the HTML file.
     *
     *
     * @param out
     *            the HTML document to write on
     * @param filename
     *            name of the inputed file
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    public static void htmlHeader(SimpleWriter out, String filename) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + filename + "</title>");
        out.println("<link href=\"http://web.cse.ohio-state.edu/software/2231/"
                + "web-sw2/assignments/projects/tag-cloud-generator/data/"
                + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("<link href=\"tagcloud.css\" "
                + "rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body><body style=\"background-color:#70839e;\">");
        out.println("<h2>Top 100 words in " + filename + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");
    }

    /**
     * Output the body of the HTML file.
     *
     * @param out
     *            The HTML document to write on
     * @param sorted
     *            alphabetized tags
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    public static void htmlBody(SimpleWriter out,
            SortingMachine<Map.Pair<String, Integer>> sorted) {
        Pair<Integer, Integer> minMax = minAndMax(sorted);
        int minCount = minMax.key();
        int maxCount = minMax.value();
        while (sorted.size() > 0) {
            Map.Pair<String, Integer> pair = sorted.removeFirst();

            int count = pair.value();

            int font = FONT_MIN;
            while (maxCount <= minCount) {
                maxCount++;
            }

            if (count > minCount) {
                font = (FONT_MAX * (count - minCount)) / (maxCount - minCount);
            }

            out.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\" title=\"count: " + pair.value() + "\">" + pair.key()
                    + "</span>");
        }
    }

    /**
     * Finds the min and max of a SortingMachine.
     *
     * @param sorted
     *            Alphabetized tags
     * @return A pair of the min and max
     */
    private static Pair<Integer, Integer> minAndMax(
            SortingMachine<Pair<String, Integer>> sorted) {
        int min = -1;
        int max = -1;
        SortingMachine<Pair<String, Integer>> temp = sorted.newInstance();
        while (sorted.size() > 0) {
            Map.Pair<String, Integer> pair = sorted.removeFirst();
            if (min > pair.value() || min == -1) {
                min = pair.value();
            }
            if (max < pair.value() || max == -1) {
                max = pair.value();
            }
            temp.add(pair);
        }
        Map<Integer, Integer> thisIsStupid = new Map1L<>();
        thisIsStupid.add(min, max);
        Pair<Integer, Integer> minMax = thisIsStupid.removeAny();
        temp.changeToExtractionMode();
        sorted.transferFrom(temp);
        //While this could return -1 for either, this does not matter
        return minMax;
    }

    /**
     * Output the footers for the HTML files.
     *
     * @param out
     *            The HTML document to write on
     * @updates out.content
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    public static void htmlFooter(SimpleWriter out) {
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
