package data_generator;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    private static String filePath = "Laubbaum.xlsx";

    public static void main(String[] args) {
        //how many instances to generate
        Instances generatedData = generateData(100);
        System.out.println(generatedData);
        ArffSaver saver = new ArffSaver();
        saver.setInstances(generatedData);
        try {
            saver.setFile(new File("Baeume.arff"));
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Instances generateData(int numOfDataSets) {
        if (numOfDataSets > 0) {
            Sheet sheet = null;
            try {
                Workbook workbook = WorkbookFactory.create(getCatalogFile());
                sheet = workbook.getSheetAt(0);
                ColumnInfo.arrayInitializer();
                columnsSize(sheet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Attribute> atts = new ArrayList<>();
            ArrayList<String> attVals;
            for (ColumnInfo columnInfo : ColumnInfo.getColumnInfoList()) {
                if (columnInfo.isNumeric()) {
                    atts.add(new Attribute(columnInfo.getName()));
                } else {
                    attVals = new ArrayList<>(columnInfo.getChildren());
                    atts.add(new Attribute(columnInfo.getName(), attVals));
                }
            }
            attVals = new ArrayList<>();
            int i = 0;
            for (Row row : sheet) {
                if (i > 1) {
                    attVals.add(row.getCell(0).getStringCellValue());
                }
                i++;
            }
            atts.add(new Attribute("Baumgattung", attVals));
            Instances data = new Instances("trees", atts, 0);
            List<Integer> randomIndices = radomIndices(numOfDataSets, sheet);
            double[] treedata;
            if (randomIndices != null) {
                for (Integer randomIndex : randomIndices) {
                    treedata = attributesHandler(randomIndex, sheet, data);
                    data.add(new DenseInstance(1.0,treedata));

                }
            }
            return data;
        } else {
            return null;
        }
    }

    private static double[] attributesHandler(int randomTree, Sheet sheet, Instances data) {
        int treeindex = randomTree + 2;
        String treename = sheet.getRow(treeindex).getCell(0).getStringCellValue();
        double[] vals = new double[data.numAttributes()];
        int infoIndex = 1;
        int valsIndex = 0;
        for (ColumnInfo columnInfo : ColumnInfo.getColumnInfoList()) {
            if (columnInfo.isNumeric()) {
                double[] arrVals = new double[2];
                int arrindex = 0;
                for (String child : columnInfo.getChildren()) {
                    double value = Double.parseDouble(sheet.getRow(treeindex).getCell(infoIndex).toString());
                    Formatter fmt = new Formatter();
                    double converter = Double.parseDouble(fmt.format("%.2f", value).toString().replace(",","."));
                    arrVals[arrindex] = converter;
                    arrindex++;
                    infoIndex++;
                }
                vals[valsIndex] = new NormalDistribution(arrVals[0],arrVals[1]).sample();
            } else {
                List<Pair<String, Double>> attPairs = new ArrayList<>();
                for (String child : columnInfo.getChildren()) {
                    Formatter fmt = new Formatter();
                    Double value = Double.parseDouble(sheet.getRow(treeindex).getCell(infoIndex).toString());
                    double fmtValue = Double.parseDouble(fmt.format("%.2f", value).toString().replace(",","."));
                    attPairs.add(new Pair<>(child,fmtValue));
                    infoIndex++;
                }
                EnumeratedDistribution<String> attDist = new EnumeratedDistribution<>(attPairs);
                String result = attDist.sample();
                vals[valsIndex] = data.attribute(columnInfo.getName()).indexOfValue(result);
            }
            valsIndex++;
        }
        vals[valsIndex] = data.attribute(valsIndex).indexOfValue(treename);
        return vals;
    }

    private static InputStream getCatalogFile() {
        return Main.class.getResourceAsStream(filePath);
    }

    private static void createColumnInfo(String tmp, int cnt, int pos, Row tmpRow) {
        String re = ".*?(\\[.*?\\])";    // Square Braces 1
        Pattern p = Pattern.compile(re, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        boolean matches = p.matcher(tmp).matches();
        List<String> children = new ArrayList<>();
        for (int i = 0; i < cnt + 1; i++) {
            String child = tmpRow.getCell(i + (pos - cnt)).getStringCellValue();
            children.add(child);
        }
        ColumnInfo columnInfo = new ColumnInfo(tmp, matches, children);
        ColumnInfo.addColumn(columnInfo);
    }

    private static void columnsSize(Sheet sheet) {
        Row row = sheet.getRow(0);
        int pos = 0;
        int cnt = 0;
        String tmp = null;
        for (Cell cell : row) {
            String val = cell.getStringCellValue();
            if (!val.equals("")) {
                if (tmp != null) {
                    createColumnInfo(tmp, cnt, pos, sheet.getRow(1));
                }
                tmp = val;
                cnt = 0;
            } else {
                cnt++;
            }
            pos++;
        }
        createColumnInfo(tmp, cnt, pos, sheet.getRow(1));
    }

    private static List<Integer> radomIndices(int size, Sheet sheet) {
        if (sheet != null) {
            int rows = sheet.getLastRowNum() - 2;
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Random random = new Random();
                list.add(random.nextInt(rows + 1));
            }
            return list;
        } else {
            return null;
        }
    }

}