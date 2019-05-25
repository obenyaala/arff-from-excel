package data_generator;

import java.util.ArrayList;
import java.util.List;

public class ColumnInfo {

    private static List<ColumnInfo> columnInfoList = new ArrayList<>();
    private String name;
    private List<String> children;
    private boolean numeric;

    public ColumnInfo(String name, boolean numeric, List<String> children) {
        this.name = name;
        this.numeric = numeric;
        this.children = new ArrayList<>(children);
    }

    public static void addColumn(ColumnInfo columnInfo){
        columnInfoList.add(columnInfo);
    }

    public static List<ColumnInfo> getColumnInfoList(){
        return new ArrayList<>(columnInfoList);
    }

    public String getName() {
        return name;
    }

    public List<String> getChildren() {
        return new ArrayList<>(children);
    }

    public boolean isNumeric() {
        return numeric;
    }

    public static void arrayInitializer(){
        columnInfoList = new ArrayList<>();
    }
}
