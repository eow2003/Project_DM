package util;

import java.io.*;
import java.util.*;

public class Converter {
	public static void csv2Arff(String csvFilePath, String arffFilePath) throws IOException {
		Map<String, String> columnTypes;
		String[] headers;

		// First reader for detecting column types
		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
			String line = br.readLine(); // Read header
			if (line == null) return;

			headers = line.split(",");
			columnTypes = detectColumnTypes(br, headers); // Detect column types
		}

		// Second reader for writing data to ARFF
		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
			 BufferedWriter bw = new BufferedWriter(new FileWriter(arffFilePath))) {

			br.readLine(); // Skip header in data rows

			// Write ARFF header
			bw.write("@RELATION auto_mpg\n\n");
			for (String header : headers) {
				String type = columnTypes.get(header);
				bw.write(String.format("@ATTRIBUTE %s %s\n", header, type));
			}
			bw.write("\n@DATA\n");

			// Write data rows
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				StringBuilder row = new StringBuilder();

				for (int i = 0; i < values.length; i++) {
					String value = values[i].trim();
					if (headers[i].equals("car_name")) {
						row.append("\"").append(value).append("\""); // Enclose car_name in quotes
					} else {
						row.append(value);
					}
					if (i < values.length - 1) row.append(",");
				}
				bw.write(row.toString() + "\n");
			}
		}
	}



	private static Map<String, String> detectColumnTypes(BufferedReader br, String[] headers) throws IOException {
		Map<String, String> columnTypes = new HashMap<>();
		int sampleSize = 100;  // Number of rows to sample for type detection
		List<String[]> samples = new ArrayList<>();

		// Collect samples
		String line;
		while ((line = br.readLine()) != null && sampleSize-- > 0) {
			samples.add(line.split(","));
		}

		// Detect type for each column based on samples
		for (int i = 0; i < headers.length; i++) {
			String header = headers[i];
			boolean isNumeric = true;
			Set<String> uniqueValues = new HashSet<>();

			for (String[] sample : samples) {
				if (i < sample.length) {
					String value = sample[i].trim();
					if (value.isEmpty() || value.equals("?")) continue;
					uniqueValues.add(value);

					// Check if numeric
					if (isNumeric && !isNumeric(value)) {
						isNumeric = false;
					}
				}
			}

			// Assign type based on analysis
			if (isNumeric) {
				columnTypes.put(header, "NUMERIC");
			} else if (uniqueValues.size() < 20) {
				columnTypes.put(header, "{" + String.join(",", uniqueValues) + "}");
			} else {
				columnTypes.put(header, "STRING");
			}
		}

		return columnTypes;
	}

	private static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
