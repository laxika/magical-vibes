package com.github.laxika.magicalvibes.repository;

import com.github.laxika.magicalvibes.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatus(String status);
}
