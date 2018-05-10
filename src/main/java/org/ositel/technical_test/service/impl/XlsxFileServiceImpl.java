package org.ositel.technical_test.service.impl;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.ositel.technical_test.domain.XlsxFile;
import org.ositel.technical_test.repository.XlsxFileRepository;
import org.ositel.technical_test.service.XlsxFileService;
import org.ositel.technical_test.service.dto.XlsxFileDTO;
import org.ositel.technical_test.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service Implementation for managing XlsxFile.
 */
@Service
@Transactional
public class XlsxFileServiceImpl implements XlsxFileService {

    private final Logger log = LoggerFactory.getLogger(XlsxFileServiceImpl.class);

    private final XlsxFileRepository xlsxFileRepository;

    public XlsxFileServiceImpl(XlsxFileRepository xlsxFileRepository) {
        this.xlsxFileRepository = xlsxFileRepository;
    }

    /**
     * Save a xlsxFile.
     *
     * @param xlsxFile the entity to save
     * @return the persisted entity
     */
    @Override
    public XlsxFile save(XlsxFile xlsxFile) {
        log.debug("Request to save XlsxFile : {}", xlsxFile);
        return xlsxFileRepository.save(xlsxFile);
    }

    /**
     * Get all the xlsxFiles.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<XlsxFile> findAll() {
        log.debug("Request to get all XlsxFiles");
        return xlsxFileRepository.findAll();
    }


    @Override
    public void uploadAndSaveXlsxFile(MultipartFile file) throws IOException {
        XlsxFile xlsxFile = new XlsxFile();
        xlsxFile.setFileName(file.getOriginalFilename());
        xlsxFile.setContent(file.getBytes());
        this.save(xlsxFile);
    }

    @Override
    public XlsxFileDTO searchExcelFileByNameAndGetItInJsonFormat(String fileName) throws InvalidFormatException, IOException {
        XlsxFile xlsxFile = this.xlsxFileRepository.findByFileName(fileName);
        XlsxFileDTO xlsxFileDTO = new XlsxFileDTO();
        if (xlsxFile == null) {
            throw new FileNotFoundException("The xlsx file with the name {" + fileName + "} requested does not exist");
        }
        xlsxFileDTO.setFileName(xlsxFile.getFileName());
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsxFile.getContent()));
        // Getting the Sheet at index zero, here we suppose that there is only one sheet to be read
        Sheet sheet = workbook.getSheetAt(0);
        boolean headerHasBeenRead = false;
        int i = 0;
        for (Iterator<Row> rowIterator = sheet.rowIterator(); rowIterator.hasNext(); i++) {
            Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
            if (!headerHasBeenRead) {
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    xlsxFileDTO.getHeaderColumns().add(cell.getStringCellValue());
                }
                // set the boolean variable to true because in the headder has been read
                if (!headerHasBeenRead) {
                    headerHasBeenRead = true;
                }
            } else {
                //read the the the lines/rows situated just after the header/first-row otherwise
                xlsxFileDTO.getLinesValues().add(new ArrayList<>());
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    xlsxFileDTO.getLinesValues().get(i - 1).add(cell.getStringCellValue());
                }
            }
        }
        return xlsxFileDTO;
    }

    @Override
    public void updateCellValueForXlsxFile(String fileName, Integer column, Integer line, String value) throws IOException, InvalidFormatException {
        XlsxFile xlsxFile = this.xlsxFileRepository.findByFileName(fileName);
        if (xlsxFile == null) {
            throw new FileNotFoundException("The xlsx file with the name {" + fileName + "} requested does not exist");
        }
        Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsxFile.getContent()));
        //As usual here we suppose that there is only one sheet to be read
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(line);
        if (row == null) {
            throw new BadRequestAlertException("Line you want to update does not exist", "XlsxFile", "column.doesNotExist");
        }
        Cell cellToUpdate = row.getCell(column);
        if (cellToUpdate == null) {
            throw new BadRequestAlertException("The colum you want to update does not exist", "XlsxFile", "line.doesNotExist");
        }
        // Here we suppose that all values in the xlsx file are of string type
        cellToUpdate.setCellValue(value);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        byteArrayOutputStream.close();
        xlsxFile.setContent(byteArrayOutputStream.toByteArray());
        log.debug("The xlsx file {} is beeing upddated by the new value {} at the indexex (col,line) ({},{})", fileName, value, column, line);
        this.save(xlsxFile);
    }
}
