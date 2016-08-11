package com.alpha.ap10.repository.search;

import com.alpha.ap10.domain.Mobile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Mobile entity.
 */
public interface MobileSearchRepository extends ElasticsearchRepository<Mobile, Long> {
}
