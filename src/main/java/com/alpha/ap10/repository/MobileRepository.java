package com.alpha.ap10.repository;

import com.alpha.ap10.domain.Mobile;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Mobile entity.
 */
@SuppressWarnings("unused")
public interface MobileRepository extends JpaRepository<Mobile,Long> {

}
