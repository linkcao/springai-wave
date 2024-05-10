package com.linkcao.repository;

import com.linkcao.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuestionsRepository extends JpaRepository<Questions,Long> {
}
