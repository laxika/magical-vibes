package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.entity.Deck;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.networking.message.DeckInfo;
import com.github.laxika.magicalvibes.networking.message.SaveDeckRequest;
import com.github.laxika.magicalvibes.networking.message.SaveDeckResponse;
import com.github.laxika.magicalvibes.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeckService {

    private static final String CUSTOM_DECK_PREFIX = "custom-";

    private final DeckRepository deckRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SaveDeckResponse saveDeck(UUID userId, SaveDeckRequest request) {
        validateCardCounts(request.entries());

        try {
            String deckJson = objectMapper.writeValueAsString(request.entries());
            Deck deck = new Deck(userId, request.name(), deckJson);
            deckRepository.save(deck);

            DeckInfo deckInfo = new DeckInfo(CUSTOM_DECK_PREFIX + deck.getId(), request.name() + " (Custom)");
            log.info("Custom deck saved: id={}, name='{}', userId={}", deck.getId(), request.name(), userId);
            return SaveDeckResponse.success(deckInfo);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save deck", e);
        }
    }

    private void validateCardCounts(List<SaveDeckRequest.DeckEntryInfo> entries) {
        Map<String, Integer> countByName = new HashMap<>();
        for (SaveDeckRequest.DeckEntryInfo entry : entries) {
            CardSet cardSet = Arrays.stream(CardSet.values())
                    .filter(s -> s.getCode().equals(entry.setCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown set code: " + entry.setCode()));
            CardPrinting printing = cardSet.findByCollectorNumber(entry.collectorNumber());
            Card card = printing.createCard();

            // Basic lands are exempt from the 4-copy rule
            if (card.getSupertypes().contains(CardSupertype.BASIC)) {
                continue;
            }

            countByName.merge(card.getName(), entry.count(), Integer::sum);
        }

        for (Map.Entry<String, Integer> e : countByName.entrySet()) {
            if (e.getValue() > 4) {
                throw new IllegalArgumentException("Maximum 4 copies of \"" + e.getKey() + "\" allowed");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<DeckInfo> getCustomDecksForUser(UUID userId) {
        return deckRepository.findByUserId(userId).stream()
                .map(d -> new DeckInfo(CUSTOM_DECK_PREFIX + d.getId(), d.getName() + " (Custom)"))
                .toList();
    }

    public boolean isCustomDeck(String deckId) {
        return deckId != null && deckId.startsWith(CUSTOM_DECK_PREFIX);
    }

    @Transactional(readOnly = true)
    public List<Card> buildCustomDeck(String deckId) {
        String uuidStr = deckId.substring(CUSTOM_DECK_PREFIX.length());
        UUID id = UUID.fromString(uuidStr);
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Custom deck not found: " + deckId));

        try {
            SaveDeckRequest.DeckEntryInfo[] entries = objectMapper.readValue(
                    deck.getDeckJson(), SaveDeckRequest.DeckEntryInfo[].class);

            List<Card> cards = new ArrayList<>();
            for (SaveDeckRequest.DeckEntryInfo entry : entries) {
                CardSet cardSet = Arrays.stream(CardSet.values())
                        .filter(s -> s.getCode().equals(entry.setCode()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown set code: " + entry.setCode()));
                CardPrinting printing = cardSet.findByCollectorNumber(entry.collectorNumber());
                for (int i = 0; i < entry.count(); i++) {
                    cards.add(printing.createCard());
                }
            }
            return cards;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build custom deck: " + deckId, e);
        }
    }
}
