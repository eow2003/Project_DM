package cmd;

import util.Converter;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import java.io.*;
import java.util.*;

public class HandleMissingDataCommand implements Command {
	private static final double MISSING_VALUE_THRESHOLD = 0.1; // 10% threshold
	private static final String ARFF_FILE_PATH = "data/removed_outliers_auto-mpg.arff"; // File sau khi xử lý outliers
	private static final String CLEANED_CSV_FILE = "data/auto-mpg-cleaned.csv";
	private static final String CLEANED_ARFF_FILE = "data/auto-mpg-cleaned.arff";

	public void exec() {
		List<String[]> data = new ArrayList<>();
		int columnCount = 0;

		try {
			// Load data from the ARFF file that has been processed for outliers
			ArffLoader loader = new ArffLoader();
			loader.setSource(new File(ARFF_FILE_PATH));
			Instances dataset = loader.getDataSet();

			// Convert Instances to CSV data
			columnCount = dataset.numAttributes();
			String[] headers = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				headers[i] = dataset.attribute(i).name();
			}
			data.add(headers);

			// Prepare structures for counting non-missing values and calculating sums
			int[] missingCounts = new int[columnCount];
			int[] validCounts = new int[columnCount];
			double[] sums = new double[columnCount];

			// Collect data and calculate missing value counts and sums for each column
			for (int i = 0; i < dataset.numInstances(); i++) {
				String[] row = new String[columnCount];
				for (int j = 0; j < columnCount; j++) {
					if (dataset.instance(i).isMissing(j)) {
						row[j] = "?";
						missingCounts[j]++;
					} else {
						row[j] = dataset.instance(i).toString(j);
						try {
							double numericValue = Double.parseDouble(row[j]);
							sums[j] += numericValue;
							validCounts[j]++;
						} catch (NumberFormatException e) {
							// Ignore non-numeric fields
						}
					}
				}
				data.add(row);
			}

			// Calculate means for numeric columns
			double[] means = new double[columnCount];
			for (int i = 0; i < columnCount; i++) {
				if (validCounts[i] > 0) {
					means[i] = sums[i] / validCounts[i];
				}
			}

			// Filter or replace missing values based on the threshold
			List<String[]> cleanedData = new ArrayList<>();
			cleanedData.add(headers); // Add header

			for (int i = 1; i < data.size(); i++) { // Start from 1 to skip header
				String[] row = data.get(i);
				boolean rowHasTooManyMissingValues = false;

				for (int j = 0; j < row.length; j++) {
					String cell = row[j].trim();
					if (cell.equals("?")) {
						double missingPercentage = (double) missingCounts[j] / (data.size() - 1); // -1 to exclude header
						if (missingPercentage < MISSING_VALUE_THRESHOLD) {
							rowHasTooManyMissingValues = true; // Mark row for removal
							break;
						} else if (validCounts[j] > 0) {
							// Replace missing value with mean if numeric
							row[j] = String.valueOf(means[j]);
						} else {
							// If no valid data in column, leave as "NaN"
							row[j] = "NaN";
						}
					}
				}

				if (!rowHasTooManyMissingValues) {
					cleanedData.add(row); // Add row if it does not have to be removed
				}
			}

			// Write cleaned data to a new CSV file
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(CLEANED_CSV_FILE))) {
				for (String[] row : cleanedData) {
					bw.write(String.join(",", row));
					bw.newLine();
				}
			}

			// Convert cleaned CSV to ARFF
			convertCsvToArff(CLEANED_CSV_FILE, CLEANED_ARFF_FILE);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to invoke CSV to ARFF conversion
	private void convertCsvToArff(String csvFilePath, String arffFilePath) {
		try {
			Converter.csv2Arff(csvFilePath, arffFilePath);
			System.out.println("ARFF file generated: " + arffFilePath);
		} catch (IOException e) {
			System.err.println("Error converting CSV to ARFF: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		HandleMissingDataCommand cmd = new HandleMissingDataCommand();
		cmd.exec();
	}
}
