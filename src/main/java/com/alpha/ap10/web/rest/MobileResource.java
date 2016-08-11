package com.alpha.ap10.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.alpha.ap10.domain.Mobile;
import com.alpha.ap10.repository.MobileRepository;
import com.alpha.ap10.repository.search.MobileSearchRepository;
import com.alpha.ap10.web.rest.util.HeaderUtil;
import com.alpha.ap10.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Mobile.
 */
@RestController
@RequestMapping("/api")
public class MobileResource {

    private final Logger log = LoggerFactory.getLogger(MobileResource.class);
        
    @Inject
    private MobileRepository mobileRepository;
    
    @Inject
    private MobileSearchRepository mobileSearchRepository;
    
    /**
     * POST  /mobiles : Create a new mobile.
     *
     * @param mobile the mobile to create
     * @return the ResponseEntity with status 201 (Created) and with body the new mobile, or with status 400 (Bad Request) if the mobile has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/mobiles",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mobile> createMobile(@Valid @RequestBody Mobile mobile) throws URISyntaxException {
        log.debug("REST request to save Mobile : {}", mobile);
        if (mobile.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("mobile", "idexists", "A new mobile cannot already have an ID")).body(null);
        }
        Mobile result = mobileRepository.save(mobile);
        mobileSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/mobiles/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("mobile", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /mobiles : Updates an existing mobile.
     *
     * @param mobile the mobile to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated mobile,
     * or with status 400 (Bad Request) if the mobile is not valid,
     * or with status 500 (Internal Server Error) if the mobile couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/mobiles",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mobile> updateMobile(@Valid @RequestBody Mobile mobile) throws URISyntaxException {
        log.debug("REST request to update Mobile : {}", mobile);
        if (mobile.getId() == null) {
            return createMobile(mobile);
        }
        Mobile result = mobileRepository.save(mobile);
        mobileSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("mobile", mobile.getId().toString()))
            .body(result);
    }

    /**
     * GET  /mobiles : get all the mobiles.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of mobiles in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/mobiles",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Mobile>> getAllMobiles(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Mobiles");
        Page<Mobile> page = mobileRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/mobiles");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /mobiles/:id : get the "id" mobile.
     *
     * @param id the id of the mobile to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the mobile, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/mobiles/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mobile> getMobile(@PathVariable Long id) {
        log.debug("REST request to get Mobile : {}", id);
        Mobile mobile = mobileRepository.findOne(id);
        return Optional.ofNullable(mobile)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /mobiles/:id : delete the "id" mobile.
     *
     * @param id the id of the mobile to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/mobiles/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMobile(@PathVariable Long id) {
        log.debug("REST request to delete Mobile : {}", id);
        mobileRepository.delete(id);
        mobileSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("mobile", id.toString())).build();
    }

    /**
     * SEARCH  /_search/mobiles?query=:query : search for the mobile corresponding
     * to the query.
     *
     * @param query the query of the mobile search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/mobiles",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Mobile>> searchMobiles(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Mobiles for query {}", query);
        Page<Mobile> page = mobileSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/mobiles");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
