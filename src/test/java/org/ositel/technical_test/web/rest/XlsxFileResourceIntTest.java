package org.ositel.technical_test.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.ositel.technical_test.OsitelApp;
import org.ositel.technical_test.domain.XlsxFile;
import org.ositel.technical_test.repository.XlsxFileRepository;
import org.ositel.technical_test.service.XlsxFileService;
import org.ositel.technical_test.web.rest.errors.ExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the XlsxFileResource REST controller.
 *
 * @see XlsxFileResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OsitelApp.class)
public class XlsxFileResourceIntTest {

    private static final String DEFAULT_FILE_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FILE_NAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_CONTENT = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_CONTENT = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_CONTENT_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_CONTENT_CONTENT_TYPE = "image/png";

    @Autowired
    private XlsxFileRepository xlsxFileRepository;

    @Autowired
    private XlsxFileService xlsxFileService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restXlsxFileMockMvc;

    private XlsxFile xlsxFile;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final XlsxFileResource xlsxFileResource = new XlsxFileResource(xlsxFileService);
        this.restXlsxFileMockMvc = MockMvcBuilders.standaloneSetup(xlsxFileResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static XlsxFile createEntity(EntityManager em) {
        XlsxFile xlsxFile = new XlsxFile()
            .fileName(DEFAULT_FILE_NAME)
            .content(DEFAULT_CONTENT);
        return xlsxFile;
    }

    @Before
    public void initTest() {
        xlsxFile = createEntity(em);
    }

    @Test
    @Transactional
    public void createXlsxFile() throws Exception {
        int databaseSizeBeforeCreate = xlsxFileRepository.findAll().size();

        // Create the XlsxFile
        restXlsxFileMockMvc.perform(post("/api/xlsx-files")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(xlsxFile)))
            .andExpect(status().isCreated());

        // Validate the XlsxFile in the database
        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeCreate + 1);
        XlsxFile testXlsxFile = xlsxFileList.get(xlsxFileList.size() - 1);
        assertThat(testXlsxFile.getFileName()).isEqualTo(DEFAULT_FILE_NAME);
        assertThat(testXlsxFile.getContent()).isEqualTo(DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    public void createXlsxFileWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = xlsxFileRepository.findAll().size();

        // Create the XlsxFile with an existing ID
        xlsxFile.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restXlsxFileMockMvc.perform(post("/api/xlsx-files")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(xlsxFile)))
            .andExpect(status().isBadRequest());

        // Validate the XlsxFile in the database
        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkFileNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = xlsxFileRepository.findAll().size();
        // set the field null
        xlsxFile.setFileName(null);

        // Create the XlsxFile, which fails.

        restXlsxFileMockMvc.perform(post("/api/xlsx-files")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(xlsxFile)))
            .andExpect(status().isBadRequest());

        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllXlsxFiles() throws Exception {
        // Initialize the database
        xlsxFileRepository.saveAndFlush(xlsxFile);

        // Get all the xlsxFileList
        restXlsxFileMockMvc.perform(get("/api/xlsx-files?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(xlsxFile.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileName").value(hasItem(DEFAULT_FILE_NAME.toString())))
            .andExpect(jsonPath("$.[*].contentContentType").value(hasItem(DEFAULT_CONTENT_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(Base64Utils.encodeToString(DEFAULT_CONTENT))));
    }

    @Test
    @Transactional
    public void getXlsxFile() throws Exception {
        // Initialize the database
        xlsxFileRepository.saveAndFlush(xlsxFile);

        // Get the xlsxFile
        restXlsxFileMockMvc.perform(get("/api/xlsx-files/{id}", xlsxFile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(xlsxFile.getId().intValue()))
            .andExpect(jsonPath("$.fileName").value(DEFAULT_FILE_NAME.toString()))
            .andExpect(jsonPath("$.contentContentType").value(DEFAULT_CONTENT_CONTENT_TYPE))
            .andExpect(jsonPath("$.content").value(Base64Utils.encodeToString(DEFAULT_CONTENT)));
    }

    @Test
    @Transactional
    public void getNonExistingXlsxFile() throws Exception {
        // Get the xlsxFile
        restXlsxFileMockMvc.perform(get("/api/xlsx-files/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateXlsxFile() throws Exception {
        // Initialize the database
        xlsxFileService.save(xlsxFile);

        int databaseSizeBeforeUpdate = xlsxFileRepository.findAll().size();

        // Update the xlsxFile
        XlsxFile updatedXlsxFile = xlsxFileRepository.findOne(xlsxFile.getId());
        updatedXlsxFile
            .fileName(UPDATED_FILE_NAME)
            .content(UPDATED_CONTENT);
        restXlsxFileMockMvc.perform(put("/api/xlsx-files")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedXlsxFile)))
            .andExpect(status().isOk());

        // Validate the XlsxFile in the database
        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeUpdate);
        XlsxFile testXlsxFile = xlsxFileList.get(xlsxFileList.size() - 1);
        assertThat(testXlsxFile.getFileName()).isEqualTo(UPDATED_FILE_NAME);
        assertThat(testXlsxFile.getContent()).isEqualTo(UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void updateNonExistingXlsxFile() throws Exception {
        int databaseSizeBeforeUpdate = xlsxFileRepository.findAll().size();

        // Create the XlsxFile

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restXlsxFileMockMvc.perform(put("/api/xlsx-files")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(xlsxFile)))
            .andExpect(status().isCreated());

        // Validate the XlsxFile in the database
        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteXlsxFile() throws Exception {
        // Initialize the database
        xlsxFileService.save(xlsxFile);

        int databaseSizeBeforeDelete = xlsxFileRepository.findAll().size();

        // Get the xlsxFile
        restXlsxFileMockMvc.perform(delete("/api/xlsx-files/{id}", xlsxFile.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<XlsxFile> xlsxFileList = xlsxFileRepository.findAll();
        assertThat(xlsxFileList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(XlsxFile.class);
        XlsxFile xlsxFile1 = new XlsxFile();
        xlsxFile1.setId(1L);
        XlsxFile xlsxFile2 = new XlsxFile();
        xlsxFile2.setId(xlsxFile1.getId());
        assertThat(xlsxFile1).isEqualTo(xlsxFile2);
        xlsxFile2.setId(2L);
        assertThat(xlsxFile1).isNotEqualTo(xlsxFile2);
        xlsxFile1.setId(null);
        assertThat(xlsxFile1).isNotEqualTo(xlsxFile2);
    }
}
