package com.linkcao.repository;

import com.linkcao.entity.Answers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AnswersRepository extends JpaRepository<Answers,Long> {
}
