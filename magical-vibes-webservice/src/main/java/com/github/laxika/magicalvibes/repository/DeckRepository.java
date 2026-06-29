package com.github.laxika.magicalvibes.repository;

import com.github.laxika.magicalvibes.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID> {

    List<Deck> findByUserId(UUID userId);
}
