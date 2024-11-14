package cmd;

import weka.core.Instances;
import weka.core.AttributeStats;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.BestFirst;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;

public class ExploratoryDataAnalysisAndFeatureSelection {

    public static void main(String[] args) {
        try {
            // Load the ARFF file for EDA and Feature Selection
            String arffFilePath = "data/outlier_handled_auto-mpg.arff";
            DataSource source = new DataSource(arffFilePath);
            Instances data = source.getDataSet();

            System.out.println("=== Exploratory Data Analysis (EDA) ===");
            System.out.println("File ARFF: " + arffFilePath);

            // Set the class index to the last attribute
            data.setClassIndex(data.numAttributes() - 1);

            // Summary of data
            System.out.println("\n1. Data Overview:");
            System.out.println(" - Instances: " + data.numInstances());
            System.out.println(" - Attributes: " + data.numAttributes());

            // Basic statistics for numeric attributes
            System.out.println("\n2. Basic Statistics:");
            for (int i = 0; i < data.numAttributes(); i++) {
                if (data.attribute(i).isNumeric()) {
                    AttributeStats stats = data.attributeStats(i);
                    System.out.println(" - " + data.attribute(i).name() + ":");
                    System.out.println("   + Mean: " + stats.numericStats.mean);
                    System.out.println("   + Std Dev: " + stats.numericStats.stdDev);
                    System.out.println("   + Min: " + stats.numericStats.min);
                    System.out.println("   + Max: " + stats.numericStats.max);
                }
            }

            // Correlation analysis for numeric attributes
            System.out.println("\n3. Correlation with Target:");
            for (int i = 0; i < data.numAttributes() - 1; i++) {
                if (data.attribute(i).isNumeric()) {
                    double correlation = Utils.correlation(
                            data.attributeToDoubleArray(i),
                            data.attributeToDoubleArray(data.classIndex()),
                            data.numInstances()
                    );
                    System.out.println(" - Correlation between " + data.attribute(i).name() + " and target: " + correlation);
                }
            }

            // Remove string attributes before Feature Selection
            System.out.println("\nRemoving string attributes for feature selection...");
            Remove removeFilter = new Remove();
            StringBuilder indicesToRemove = new StringBuilder();
            for (int i = 0; i < data.numAttributes(); i++) {
                if (data.attribute(i).isString()) {
                    indicesToRemove.append(i + 1).append(","); // Weka uses 1-based indexing
                }
            }
            if (indicesToRemove.length() > 0) {
                // Set indices to remove string attributes
                removeFilter.setAttributeIndices(indicesToRemove.toString());
                removeFilter.setInputFormat(data);
                data = Filter.useFilter(data, removeFilter);
            }
            System.out.println("String attributes removed.");

            System.out.println("\n=== Feature Selection ===");

            // Feature selection using CfsSubsetEval and BestFirst
            AttributeSelection selector = new AttributeSelection();
            CfsSubsetEval evaluator = new CfsSubsetEval();
            BestFirst search = new BestFirst();

            selector.setEvaluator(evaluator);
            selector.setSearch(search);
            selector.SelectAttributes(data);

            Instances selectedData = selector.reduceDimensionality(data);

            System.out.println("Selected attributes: ");
            for (int i = 0; i < selectedData.numAttributes(); i++) {
                System.out.println(" - " + selectedData.attribute(i).name());
            }

            // Save the reduced dataset with selected features
            String selectedArffFilePath = "data/selected_features_auto-mpg.arff";
            ArffSaver saver = new ArffSaver();
            saver.setInstances(selectedData);
            saver.setFile(new File(selectedArffFilePath));
            saver.writeBatch();

            System.out.println("Selected feature dataset saved to: " + selectedArffFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
