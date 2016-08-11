package com.alpha.ap10.web.rest;

import com.alpha.ap10.Ap10App;
import com.alpha.ap10.domain.Mobile;
import com.alpha.ap10.repository.MobileRepository;
import com.alpha.ap10.repository.search.MobileSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the MobileResource REST controller.
 *
 * @see MobileResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Ap10App.class)
@WebAppConfiguration
@IntegrationTest
public class MobileResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_MODEL = "AAAAA";
    private static final String UPDATED_MODEL = "BBBBB";

    @Inject
    private MobileRepository mobileRepository;

    @Inject
    private MobileSearchRepository mobileSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMobileMockMvc;

    private Mobile mobile;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MobileResource mobileResource = new MobileResource();
        ReflectionTestUtils.setField(mobileResource, "mobileSearchRepository", mobileSearchRepository);
        ReflectionTestUtils.setField(mobileResource, "mobileRepository", mobileRepository);
        this.restMobileMockMvc = MockMvcBuilders.standaloneSetup(mobileResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        mobileSearchRepository.deleteAll();
        mobile = new Mobile();
        mobile.setName(DEFAULT_NAME);
        mobile.setModel(DEFAULT_MODEL);
    }

    @Test
    @Transactional
    public void createMobile() throws Exception {
        int databaseSizeBeforeCreate = mobileRepository.findAll().size();

        // Create the Mobile

        restMobileMockMvc.perform(post("/api/mobiles")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mobile)))
                .andExpect(status().isCreated());

        // Validate the Mobile in the database
        List<Mobile> mobiles = mobileRepository.findAll();
        assertThat(mobiles).hasSize(databaseSizeBeforeCreate + 1);
        Mobile testMobile = mobiles.get(mobiles.size() - 1);
        assertThat(testMobile.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMobile.getModel()).isEqualTo(DEFAULT_MODEL);

        // Validate the Mobile in ElasticSearch
        Mobile mobileEs = mobileSearchRepository.findOne(testMobile.getId());
        assertThat(mobileEs).isEqualToComparingFieldByField(testMobile);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = mobileRepository.findAll().size();
        // set the field null
        mobile.setName(null);

        // Create the Mobile, which fails.

        restMobileMockMvc.perform(post("/api/mobiles")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mobile)))
                .andExpect(status().isBadRequest());

        List<Mobile> mobiles = mobileRepository.findAll();
        assertThat(mobiles).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMobiles() throws Exception {
        // Initialize the database
        mobileRepository.saveAndFlush(mobile);

        // Get all the mobiles
        restMobileMockMvc.perform(get("/api/mobiles?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(mobile.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL.toString())));
    }

    @Test
    @Transactional
    public void getMobile() throws Exception {
        // Initialize the database
        mobileRepository.saveAndFlush(mobile);

        // Get the mobile
        restMobileMockMvc.perform(get("/api/mobiles/{id}", mobile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(mobile.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMobile() throws Exception {
        // Get the mobile
        restMobileMockMvc.perform(get("/api/mobiles/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMobile() throws Exception {
        // Initialize the database
        mobileRepository.saveAndFlush(mobile);
        mobileSearchRepository.save(mobile);
        int databaseSizeBeforeUpdate = mobileRepository.findAll().size();

        // Update the mobile
        Mobile updatedMobile = new Mobile();
        updatedMobile.setId(mobile.getId());
        updatedMobile.setName(UPDATED_NAME);
        updatedMobile.setModel(UPDATED_MODEL);

        restMobileMockMvc.perform(put("/api/mobiles")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedMobile)))
                .andExpect(status().isOk());

        // Validate the Mobile in the database
        List<Mobile> mobiles = mobileRepository.findAll();
        assertThat(mobiles).hasSize(databaseSizeBeforeUpdate);
        Mobile testMobile = mobiles.get(mobiles.size() - 1);
        assertThat(testMobile.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMobile.getModel()).isEqualTo(UPDATED_MODEL);

        // Validate the Mobile in ElasticSearch
        Mobile mobileEs = mobileSearchRepository.findOne(testMobile.getId());
        assertThat(mobileEs).isEqualToComparingFieldByField(testMobile);
    }

    @Test
    @Transactional
    public void deleteMobile() throws Exception {
        // Initialize the database
        mobileRepository.saveAndFlush(mobile);
        mobileSearchRepository.save(mobile);
        int databaseSizeBeforeDelete = mobileRepository.findAll().size();

        // Get the mobile
        restMobileMockMvc.perform(delete("/api/mobiles/{id}", mobile.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean mobileExistsInEs = mobileSearchRepository.exists(mobile.getId());
        assertThat(mobileExistsInEs).isFalse();

        // Validate the database is empty
        List<Mobile> mobiles = mobileRepository.findAll();
        assertThat(mobiles).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMobile() throws Exception {
        // Initialize the database
        mobileRepository.saveAndFlush(mobile);
        mobileSearchRepository.save(mobile);

        // Search the mobile
        restMobileMockMvc.perform(get("/api/_search/mobiles?query=id:" + mobile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mobile.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL.toString())));
    }
}
