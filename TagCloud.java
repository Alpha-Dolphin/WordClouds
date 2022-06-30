import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import components.map.Map;
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
public final class TagCloud {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloud() {
    }

    /**
     * minimum word count.
     */
    static final int MIN_COUNT = 1;

    /**
     * maximum word count.
     */
    static final int MAX_COUNT = 1000;

    /**
     * minimum font size.
     */
    static final int FONT_MIN = 11;

    /**
     * maximum font size.
     */
    static final int MAX_FONT_SIZE = 78;

    /**
     * Compare {@code key}s in alphabetical order.
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
     * Main method.
     *
     * @param args
     *            The command line arguments; unused here
     */
    public static void main(String[] args) {
        //Open I/O streams.
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        //Input file and output folder.
        out.print("Enter a valid text file: ");
        String inputFileName = in.nextLine();
        SimpleReader inFile = new SimpleReader1L(inputFileName);
        out.print("Enter an output file name: ");
        String outputFile = in.nextLine();
        out.print("Enter how many words you want to be included: ");
        int cloudSize = in.nextInteger();
        SimpleWriter output = new SimpleWriter1L(outputFile);
        Map<String, Integer> reducedMap = mapGenerator(inFile, cloudSize);
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
    }

    /**
     * Method to generate a map of the cloudSizeth most common tags and the
     * amount of times they occur.
     *
     * @param inFile
     *            The file input stream
     * @param cloudSize
     *            The number of tags to be generated
     * @return the map of the most common cloudSize elements and their
     *         occurrences
     */
    private static Map<String, Integer> mapGenerator(SimpleReader inFile,
            int cloudSize) {
        //List to keep count of the occurrences of the cloudSizeth most items
        List<Integer> most = new ArrayList<Integer>();
        //Map of all the tags
        Map<String, Integer> tags = new Map1L<>();
        //Map to be returned of the cloudSizeth most items
        Map<String, Integer> thisMap = new Map1L<>();
        while (!inFile.atEOS()) {
            String line = inFile.nextLine() + '!';
            final String separators = " ,\t\n\r-.!?[]';:/()";
            String nextTag = "";
            do {
                char c = line.charAt(0);
                if (separators.indexOf(c) == -1) {
                    nextTag += c;
                } else if (nextTag.length() > 0) {
                    nextTag = nextTag.toLowerCase();
                    if (!tags.hasKey(nextTag)) {
                        tags.add(nextTag, 1);
                        if (most.size() < cloudSize) {
                            most.add(1);
                        }
                    } else {
                        int value = tags.value(nextTag);
                        tags.replaceValue(nextTag, value + 1);
                        if (most.contains(value)) {
                            most.remove(Integer.valueOf(value));
                            most.add(value + 1);
                        }
                    }
                    nextTag = "";
                }
                line = line.substring(1);
            } while (line.length() > 0);
        }
        while (!most.isEmpty()) {
            thisMap.add(tags.key(most.get(0)), most.get(0));
            tags.remove(tags.key(most.get(0)));
            most.remove(0);
        }
        return thisMap;
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
        out.println("<h2>Top 100 words in" + filename + "</h2>");
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
        while (sorted.size() > 0) {
            Map.Pair<String, Integer> pair = sorted.removeFirst();

            int count = pair.value();

            int font = FONT_MIN;

            /*
             * final static int MIN_COUNT = 1; final static int MAX_COUNT = 800;
             * final static int FONT_MIN = 11; final static int MAX_FONT_SIZE =
             * 48;
             */

            if (count > MIN_COUNT) {
                font = (MAX_FONT_SIZE * (count - MIN_COUNT))
                        / (MAX_COUNT - MIN_COUNT);
            }

            out.println("<span style=\"cursor:default\" class=\"f" + font
                    + "\" title=\"count: " + pair.value() + "\">" + pair.key()
                    + "</span>");
        }
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
