package com.alpha.ap10.web.rest;

import com.alpha.ap10.Ap10App;
import com.alpha.ap10.domain.Entry;
import com.alpha.ap10.repository.EntryRepository;
import com.alpha.ap10.repository.search.EntrySearchRepository;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the EntryResource REST controller.
 *
 * @see EntryResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Ap10App.class)
@WebAppConfiguration
@IntegrationTest
public class EntryResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAA";
    private static final String UPDATED_TITLE = "BBBBB";
    private static final String DEFAULT_CONTENT = "AAAAA";
    private static final String UPDATED_CONTENT = "BBBBB";

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    @Inject
    private EntryRepository entryRepository;

    @Inject
    private EntrySearchRepository entrySearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restEntryMockMvc;

    private Entry entry;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EntryResource entryResource = new EntryResource();
        ReflectionTestUtils.setField(entryResource, "entrySearchRepository", entrySearchRepository);
        ReflectionTestUtils.setField(entryResource, "entryRepository", entryRepository);
        this.restEntryMockMvc = MockMvcBuilders.standaloneSetup(entryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        entrySearchRepository.deleteAll();
        entry = new Entry();
        entry.setTitle(DEFAULT_TITLE);
        entry.setContent(DEFAULT_CONTENT);
        entry.setDate(DEFAULT_DATE);
    }

    @Test
    @Transactional
    public void createEntry() throws Exception {
        int databaseSizeBeforeCreate = entryRepository.findAll().size();

        // Create the Entry

        restEntryMockMvc.perform(post("/api/entries")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(entry)))
                .andExpect(status().isCreated());

        // Validate the Entry in the database
        List<Entry> entries = entryRepository.findAll();
        assertThat(entries).hasSize(databaseSizeBeforeCreate + 1);
        Entry testEntry = entries.get(entries.size() - 1);
        assertThat(testEntry.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testEntry.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testEntry.getDate()).isEqualTo(DEFAULT_DATE);

        // Validate the Entry in ElasticSearch
        Entry entryEs = entrySearchRepository.findOne(testEntry.getId());
        assertThat(entryEs).isEqualToComparingFieldByField(testEntry);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = entryRepository.findAll().size();
        // set the field null
        entry.setTitle(null);

        // Create the Entry, which fails.

        restEntryMockMvc.perform(post("/api/entries")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(entry)))
                .andExpect(status().isBadRequest());

        List<Entry> entries = entryRepository.findAll();
        assertThat(entries).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = entryRepository.findAll().size();
        // set the field null
        entry.setContent(null);

        // Create the Entry, which fails.

        restEntryMockMvc.perform(post("/api/entries")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(entry)))
                .andExpect(status().isBadRequest());

        List<Entry> entries = entryRepository.findAll();
        assertThat(entries).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEntries() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get all the entries
        restEntryMockMvc.perform(get("/api/entries?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(entry.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
                .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())));
    }

    @Test
    @Transactional
    public void getEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);

        // Get the entry
        restEntryMockMvc.perform(get("/api/entries/{id}", entry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(entry.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEntry() throws Exception {
        // Get the entry
        restEntryMockMvc.perform(get("/api/entries/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);
        entrySearchRepository.save(entry);
        int databaseSizeBeforeUpdate = entryRepository.findAll().size();

        // Update the entry
        Entry updatedEntry = new Entry();
        updatedEntry.setId(entry.getId());
        updatedEntry.setTitle(UPDATED_TITLE);
        updatedEntry.setContent(UPDATED_CONTENT);
        updatedEntry.setDate(UPDATED_DATE);

        restEntryMockMvc.perform(put("/api/entries")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedEntry)))
                .andExpect(status().isOk());

        // Validate the Entry in the database
        List<Entry> entries = entryRepository.findAll();
        assertThat(entries).hasSize(databaseSizeBeforeUpdate);
        Entry testEntry = entries.get(entries.size() - 1);
        assertThat(testEntry.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testEntry.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testEntry.getDate()).isEqualTo(UPDATED_DATE);

        // Validate the Entry in ElasticSearch
        Entry entryEs = entrySearchRepository.findOne(testEntry.getId());
        assertThat(entryEs).isEqualToComparingFieldByField(testEntry);
    }

    @Test
    @Transactional
    public void deleteEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);
        entrySearchRepository.save(entry);
        int databaseSizeBeforeDelete = entryRepository.findAll().size();

        // Get the entry
        restEntryMockMvc.perform(delete("/api/entries/{id}", entry.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean entryExistsInEs = entrySearchRepository.exists(entry.getId());
        assertThat(entryExistsInEs).isFalse();

        // Validate the database is empty
        List<Entry> entries = entryRepository.findAll();
        assertThat(entries).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEntry() throws Exception {
        // Initialize the database
        entryRepository.saveAndFlush(entry);
        entrySearchRepository.save(entry);

        // Search the entry
        restEntryMockMvc.perform(get("/api/_search/entries?query=id:" + entry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(entry.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())));
    }
}
