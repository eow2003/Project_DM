package cmd;

import util.Converter;
import java.io.IOException;  // Import IOException

public class OriginalCsv2ArffCommand implements Command {
	// Paths for input and output files
	public final static String ORIGINAL_CSV_DATASET = "data/auto-mpg.csv";  // Input CSV path
	public final static String ORIGINAL_ARFF_DATASET = "data/auto-mpg.arff";  // Output ARFF path

	public void exec() {
		try {
			Converter.csv2Arff(ORIGINAL_CSV_DATASET, ORIGINAL_ARFF_DATASET);
		} catch (IOException e) {
			System.err.println("Error converting CSV to ARFF: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Command cmd = new OriginalCsv2ArffCommand();
		cmd.exec();
	}
}
