package com.github.laxika.magicalvibes.repository;

import com.github.laxika.magicalvibes.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    boolean existsByGameIdAndUserId(Long gameId, Long userId);

    Optional<GamePlayer> findByGameIdAndUserId(Long gameId, Long userId);
}
