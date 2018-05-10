package org.ositel.technical_test.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.ositel.technical_test.domain.XlsxFile;
import org.ositel.technical_test.service.dto.XlsxFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Service Interface for managing XlsxFile.
 */
public interface XlsxFileService {

    /**
     * Save a xlsxFile.
     *
     * @param xlsxFile the entity to save
     * @return the persisted entity
     */
    XlsxFile save(XlsxFile xlsxFile);

    /**
     * Get all the xlsxFiles.
     *
     * @return the list of entities
     */
    List<XlsxFile> findAll();

    /**
     * method used to upload xlsx file
     *
     * @param file
     * @throws IOException
     */
    void uploadAndSaveXlsxFile(MultipartFile file) throws IOException;

    /**
     * method used to search xlsx file by name,
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException if the file has not been foud in H2 db
     */
    XlsxFileDTO searchExcelFileByNameAndGetItInJsonFormat(String fileName) throws InvalidFormatException, IOException;

    /**
     * used to update cell value
     * @param fileName name of the xlsx file
     * @param column columnn index
     * @param line line index
     * @throws IOException
     */
    void updateCellValueForXlsxFile(String fileName, Integer column, Integer line, String value) throws IOException, InvalidFormatException;
}
