package org.ositel.technical_test.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.ositel.technical_test.domain.XlsxFile;
import org.ositel.technical_test.service.XlsxFileService;
import org.ositel.technical_test.service.dto.XlsxFileDTO;
import org.ositel.technical_test.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing XlsxFile.
 */
@RestController
@RequestMapping("/api/ositel")
public class XlsxFileResource {

    private final Logger log = LoggerFactory.getLogger(XlsxFileResource.class);

    private static final String ENTITY_NAME = "xlsxFile";

    private final XlsxFileService xlsxFileService;

    public XlsxFileResource(XlsxFileService xlsxFileService) {
        this.xlsxFileService = xlsxFileService;
    }

    /**
     * just for test to request all xlsx files that have been uploaded till now and saved in H2 db
     *
     * @return
     */
    @GetMapping("/xlsx-files")
    @Timed
    public List<XlsxFile> getAllXlsxFiles() {
        log.debug("REST request to get all XlsxFiles");
        return xlsxFileService.findAll();
    }

    /* Ositel requested ws will be placed here*/

    /**
     * @param file
     * @return
     */
    @PostMapping("/uploadExcelFile")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.debug("uploading file  now ...");
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "error.emptyFile", "please select a file!")).build();
        }
        log.debug("saving the xlsx file now ...");
        try {
            this.xlsxFileService.uploadAndSaveXlsxFile(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(HeaderUtil.createFailureAlert(ENTITY_NAME, file.getOriginalFilename(),
                "Internal server error has occurred when the application was trying to save the xlsx file")).build();
        }
        return new ResponseEntity("Successfully uploaded - " + file.getOriginalFilename(),
            HeaderUtil.createEntityCreationAlert(ENTITY_NAME, file.getOriginalFilename()), HttpStatus.OK);
    }

    @GetMapping("/searchExcelFile")
    public ResponseEntity<XlsxFileDTO> searchExcelFileByNameAndGetItInJsonFormat(@RequestParam("fileName") String fileName) {
        log.debug("Request to get the xlsx file {} in json format", fileName);
        XlsxFileDTO xlsxFileDTO;
        try {
            xlsxFileDTO = this.xlsxFileService.searchExcelFileByNameAndGetItInJsonFormat(fileName);
            return ResponseEntity.ok(xlsxFileDTO);
            //FileNotFoundException is a subclass of IOException, so no need to catch it, we have only to catch IOException
        } catch (IOException | InvalidFormatException ex) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "error.exception", ex.getMessage())).build();
        }
    }

    @PutMapping("/{column}/{line}/updateCellValue")
    public ResponseEntity<?> updateCellValue(@PathVariable("column") Integer column, @PathVariable("line") Integer line, @RequestParam("fileName") String fileName, @RequestParam("newValue") String newValue) {
        log.debug("Request to updateCellValue of the xlsx file {} at colum {} and line {} with the value {}", fileName, column, line, newValue);
        try {
            this.xlsxFileService.updateCellValueForXlsxFile(fileName, column, line, newValue);
            return ResponseEntity.ok().build();
        } catch (IOException | InvalidFormatException ex) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "error.exception", ex.getMessage())).build();
        }
    }

}
