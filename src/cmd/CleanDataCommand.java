package cmd;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CleanDataCommand implements Command {
	// Các đường dẫn đến file đã làm sạch từ HandleMissingDataCommand
	public final static String CLEAN_CSV_DATASET = "data/auto-mpg-cleaned.csv";
	public final static String CLEAN_ARFF_DATASET = "data/auto-mpg-cleaned.arff";
	public final static String OUTLIER_HANDLED_CSV = "data/outlier_handled_auto-mpg.csv";
	public final static String OUTLIER_HANDLED_ARFF = "data/outlier_handled_auto-mpg.arff";

	private static final Logger logger = Logger.getLogger(CleanDataCommand.class.getName());

	public void exec() {
		try {
			// Sử dụng file đã làm sạch từ HandleMissingDataCommand
			String inputFilePath = CLEAN_ARFF_DATASET;
			File inputFile = new File(inputFilePath);
			if (!inputFile.exists()) {
				logger.log(Level.SEVERE, "The file does not exist: " + inputFilePath);
				return;
			}

			// Load the cleaned ARFF dataset
			Instances dataset = DataSource.read(inputFilePath);
			if (dataset == null) {
				logger.log(Level.SEVERE, "Failed to load dataset. Check if the file path is correct: " + inputFilePath);
				return;
			}
			System.out.println("Loaded dataset: " + dataset.relationName());

			// Step 1: Remove duplicate instances
			Instances uniqueDataset = removeDuplicates(dataset);
			System.out.println("Duplicate rows removed, remaining instances: " + uniqueDataset.numInstances());

			// Step 2: Encode categorical variables (one-hot encoding)
			Instances encodedDataset = encodeCategoricalVariables(uniqueDataset);
			System.out.println("Categorical variables encoded, resulting attributes: " + encodedDataset.numAttributes());

			// Save encoded dataset (duplicates removed, categorical variables encoded) in ARFF and CSV
			saveAsArff(encodedDataset, CLEAN_ARFF_DATASET);
			saveAsCsv(encodedDataset, CLEAN_CSV_DATASET);

			// Step 3: Handle outliers (capping based on IQR if needed)
			Instances outlierHandledDataset = handleOutliers(encodedDataset);
			System.out.println("Outliers processed, remaining instances: " + outlierHandledDataset.numInstances());

			// Save outlier-handled dataset in ARFF and CSV
			saveAsArff(outlierHandledDataset, OUTLIER_HANDLED_ARFF);
			saveAsCsv(outlierHandledDataset, OUTLIER_HANDLED_CSV);

			System.out.println("Saved datasets in both ARFF and CSV formats after processing for duplicates, encoding, and outliers.");

		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred while processing the cleaned dataset", e);
		}
	}

	// Method to remove duplicates from the dataset
	private Instances removeDuplicates(Instances dataset) {
		HashSet<String> uniqueInstances = new HashSet<>();
		Instances filteredDataset = new Instances(dataset, 0); // Create an empty dataset with the same structure

		for (int i = 0; i < dataset.numInstances(); i++) {
			String instanceString = dataset.instance(i).toString();
			if (!uniqueInstances.contains(instanceString)) {
				uniqueInstances.add(instanceString); // Track unique instances
				filteredDataset.add(dataset.instance(i)); // Add only unique instances to the new dataset
			}
		}
		return filteredDataset;
	}

	// Method to handle outliers based on IQR
	private Instances handleOutliers(Instances dataset) {
		Instances cleanedDataset = new Instances(dataset); // Copy of the dataset for modification

		for (int j = 0; j < cleanedDataset.numAttributes(); j++) {
			if (cleanedDataset.attribute(j).isNumeric()) {
				double[] values = cleanedDataset.attributeToDoubleArray(j);
				double q1 = calculatePercentile(values, 25);
				double q3 = calculatePercentile(values, 75);
				double iqr = q3 - q1;
				double lowerBound = q1 - 1.5 * iqr;
				double upperBound = q3 + 1.5 * iqr;

				// Process outliers by capping to boundary values
				for (int i = 0; i < cleanedDataset.numInstances(); i++) {
					double value = cleanedDataset.instance(i).value(j);
					if (value < lowerBound) {
						cleanedDataset.instance(i).setValue(j, lowerBound);
					} else if (value > upperBound) {
						cleanedDataset.instance(i).setValue(j, upperBound);
					}
				}
			}
		}
		return cleanedDataset;
	}

	// Helper method to calculate percentiles
	private double calculatePercentile(double[] values, double percentile) {
		Arrays.sort(values);
		int index = (int) Math.ceil(percentile / 100.0 * values.length);
		return values[Math.min(index, values.length - 1)];
	}

	// Method to encode categorical variables (One-hot encoding using Weka's NominalToBinary filter)
	private Instances encodeCategoricalVariables(Instances dataset) throws Exception {
		NominalToBinary filter = new NominalToBinary();
		filter.setInputFormat(dataset);
		return Filter.useFilter(dataset, filter);
	}

	// Method to save dataset as ARFF
	private void saveAsArff(Instances dataset, String filePath) throws Exception {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataset);
		saver.setFile(new File(filePath));
		saver.writeBatch();
		System.out.println("ARFF file saved at: " + filePath);
	}

	// Method to save dataset as CSV
	private void saveAsCsv(Instances dataset, String filePath) throws Exception {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(dataset);
		saver.setFile(new File(filePath));
		saver.writeBatch();
		System.out.println("CSV file saved at: " + filePath);
	}

	public static void main(String[] args) {
		CleanDataCommand cmd = new CleanDataCommand();
		cmd.exec();
	}
}
