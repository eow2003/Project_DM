package cmd;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.core.converters.ConverterUtils.DataSource;

public class RemoveUselessAttributes {

    public static Instances removeUselessAttributes(Instances data) throws Exception {
        // Lưu số lượng và tên cột ban đầu
        int initialNumAttributes = data.numAttributes();
        String[] initialAttributes = new String[initialNumAttributes];
        for (int i = 0; i < initialNumAttributes; i++) {
            initialAttributes[i] = data.attribute(i).name();
        }

        // Áp dụng bộ lọc RemoveUseless
        RemoveUseless removeUseless = new RemoveUseless();
        removeUseless.setInputFormat(data);
        Instances newData = Filter.useFilter(data, removeUseless);

        // In ra tên các cột bị loại bỏ
        System.out.println("Attributes removed:");
        for (String attribute : initialAttributes) {
            if (newData.attribute(attribute) == null) {
                System.out.println(" - " + attribute);
            }
        }

        System.out.println("Number of attributes after removing useless ones: " + newData.numAttributes());
        return newData;
    }

    public static void main(String[] args) {
        try {
            // Load dataset
            Instances data = DataSource.read("data/removed_outliers_auto-mpg.arff");

            // Remove useless attributes and print the removed attributes
            Instances cleanedData = removeUselessAttributes(data);

            // Save or further process cleanedData as needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
