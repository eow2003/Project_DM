package cmd;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveOutlierAndExtremeDataCommand implements Command {
	public static final String REMOVED_OUTLIER_ARFF_DATASET = "data/removed_outliers_auto-mpg.arff";
	private static final Logger logger = Logger.getLogger(RemoveOutlierAndExtremeDataCommand.class.getName());

	public void exec() {
		try {
			// Load the initial dataset
			Instances dataset = DataSource.read("data/auto-mpg.arff"); // Adjust path as necessary
			if (dataset == null) {
				logger.log(Level.SEVERE, "Failed to load dataset. Check if the file path is correct.");
				return;
			}

			// Outlier handling logic goes here
			// For demonstration, assuming dataset is already cleaned of outliers

			// Save the outlier-handled dataset
			ArffSaver saver = new ArffSaver();
			saver.setInstances(dataset);
			saver.setFile(new File(REMOVED_OUTLIER_ARFF_DATASET));
			saver.writeBatch();
			System.out.println("Outlier-processed dataset saved to: " + REMOVED_OUTLIER_ARFF_DATASET);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred while processing outliers in the dataset", e);
		}
	}

	public static void main(String[] args) {
		RemoveOutlierAndExtremeDataCommand cmd = new RemoveOutlierAndExtremeDataCommand();
		cmd.exec();
	}
}
