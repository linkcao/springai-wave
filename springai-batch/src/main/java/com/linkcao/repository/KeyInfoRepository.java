package com.linkcao.repository;

import com.linkcao.entity.KeyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface KeyInfoRepository extends JpaRepository<KeyInfo,Long> {
}
