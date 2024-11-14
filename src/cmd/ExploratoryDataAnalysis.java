package cmd;

import weka.core.Instances;
import weka.core.AttributeStats;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Utils;

public class ExploratoryDataAnalysis {

    public static void main(String[] args) {
        try {
            // Sử dụng file ARFF hoàn chỉnh nhất sau các bước xử lý
            String arffFilePath = "data/outlier_handled_auto-mpg.arff"; // Path to the processed ARFF file
            DataSource source = new DataSource(arffFilePath);
            Instances data = source.getDataSet();

            System.out.println("=== Exploratory Data Analysis (EDA) ===");
            System.out.println("File ARFF: " + arffFilePath);

            // Tổng quan về dữ liệu
            System.out.println("\n1. Data overview:");
            System.out.println(" - Number of instances: " + data.numInstances());
            System.out.println(" - Number of attributes: " + data.numAttributes());

            // Kiểm tra kiểu dữ liệu của các thuộc tính
            System.out.println("\n2. Data types of attributes:");
            for (int i = 0; i < data.numAttributes(); i++) {
                System.out.println(" - " + data.attribute(i).name() + " (Type: " + data.attribute(i).type() + ")");
            }

            // Phân tích thống kê cơ bản cho các thuộc tính dạng số
            System.out.println("\n3. Basic statistics for numeric attributes:");
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

            // Phân phối của các giá trị trong từng thuộc tính danh mục
            System.out.println("\n4. Distribution of values for nominal attributes:");
            for (int i = 0; i < data.numAttributes(); i++) {
                if (data.attribute(i).isNominal()) {
                    System.out.println(" - " + data.attribute(i).name() + " (Nominal):");
                    int[] counts = data.attributeStats(i).nominalCounts;
                    for (int j = 0; j < counts.length; j++) {
                        System.out.println("   + " + data.attribute(i).value(j) + ": " + counts[j]);
                    }
                }
            }

            // Kiểm tra giá trị thiếu
            System.out.println("\n5. Missing values in each attribute:");
            for (int i = 0; i < data.numAttributes(); i++) {
                AttributeStats stats = data.attributeStats(i);
                System.out.println(" - " + data.attribute(i).name() + ": " + stats.missingCount + " missing values");
            }

            // Tính toán tương quan giữa các thuộc tính và biến mục tiêu
            System.out.println("\n6. Correlation between attributes and target variable:");
            data.setClassIndex(data.numAttributes() - 1); // Set the last attribute as the target variable
            for (int i = 0; i < data.numAttributes() - 1; i++) {
                if (data.attribute(i).isNumeric()) { // Only calculate correlation for numeric attributes
                    double correlation = Utils.correlation(
                            data.attributeToDoubleArray(i),
                            data.attributeToDoubleArray(data.classIndex()),
                            data.numInstances() // Number of instances for correlation calculation
                    );
                    System.out.println(" - Correlation between " + data.attribute(i).name() + " and target: " + correlation);
                }
            }

            System.out.println("\n=== End of EDA ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
