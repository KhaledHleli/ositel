package org.ositel.technical_test.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XlsxFileDTO implements Serializable {

    private static final long serialVersionUID = 2L;

    private String fileName;
    private List<String> headerColumns = new ArrayList<>();
    private List<List<String>> linesValues = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getHeaderColumns() {
        return headerColumns;
    }

    public void setHeaderColumns(List<String> headerColumns) {
        this.headerColumns = headerColumns;
    }

    public List<List<String>> getLinesValues() {
        return linesValues;
    }

    public void setLinesValues(List<List<String>> linesValues) {
        this.linesValues = linesValues;
    }

    @Override
    public String toString() {
        return "XlsxFileDTO{" +
            "fileName='" + fileName + '\'' +
            ", headerColumns=" + headerColumns +
            ", linesValues=" + linesValues +
            '}';
    }
}
