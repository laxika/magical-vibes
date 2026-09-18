package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.cards.*;
import com.github.laxika.magicalvibes.entity.Deck;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.networking.message.*;
import com.github.laxika.magicalvibes.repository.DeckRepository;
import com.github.laxika.magicalvibes.service.CustomDeckSource;
import com.github.laxika.magicalvibes.service.DeckValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DeckService implements CustomDeckSource {
    private static final String PREFIX = "custom-";
    private final DeckRepository deckRepository;
    private final ObjectMapper objectMapper;
    private final CardCatalog cardCatalog;
    private final DeckValidationService validator;

    @Transactional
    public SaveDeckResponse saveDeck(UUID userId, SaveDeckRequest request) {
        if (request.name() == null || request.name().isBlank() || request.name().length() > 100)
            throw new IllegalArgumentException("Deck name must contain 1 to 100 characters");
        DeckValidation validation = validate(request);
        Deck deck = request.id() == null ? new Deck(userId, request.name().trim(), "[]") : owned(userId, request.id());
        deck.setName(request.name().trim());
        deck.setDeckJson(objectMapper.writeValueAsString(request.entries()));
        deck.setSideboardJson(objectMapper.writeValueAsString(request.sideboard()));
        deck.setCommanderJson(request.commander() == null ? null : objectMapper.writeValueAsString(request.commander()));
        deck.setFormat(request.format());
        deckRepository.save(deck);
        return SaveDeckResponse.success(new DeckInfo(PREFIX + deck.getId(), deck.getName(), deck.getFormat(), validation));
    }

    public DeckValidation validate(SaveDeckRequest request) {
        return validator.validate(resolve(request), request.format());
    }

    private DeckDefinition resolve(SaveDeckRequest request) {
        if (request.commander() != null && request.commander().count() != 1)
            throw new IllegalArgumentException("Select exactly one commander");
        List<Card> commanders = request.commander() == null ? List.of() : build(List.of(request.commander()));
        return new DeckDefinition(build(request.entries()), build(request.sideboard()), commanders.isEmpty() ? null : commanders.getFirst());
    }

    private List<Card> build(List<SaveDeckRequest.DeckEntryInfo> entries) {
        List<Card> cards = new ArrayList<>();
        for (var entry : entries) {
            if (entry.count() <= 0 || entry.count() > 1000 || cards.size() + entry.count() > 2000)
                throw new IllegalArgumentException("Invalid card count");
            CardSet set = CardSet.findByCode(entry.setCode());
            if (set == null) throw new IllegalArgumentException("Unknown set: " + entry.setCode());
            CardPrinting printing = cardCatalog.findByCollectorNumber(set, entry.collectorNumber());
            if (printing == null) throw new IllegalArgumentException("Card is not implemented: " + entry.setCode() + " " + entry.collectorNumber());
            for (int i = 0; i < entry.count(); i++) cards.add(printing.createCard());
        }
        return cards;
    }

    private Deck find(String id) {
        if (!isCustomDeck(id)) throw new IllegalArgumentException("Not a custom deck");
        return deckRepository.findById(UUID.fromString(id.substring(PREFIX.length())))
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));
    }
    private Deck owned(UUID user, String id) {
        Deck deck = find(id);
        if (!user.equals(deck.getUserId())) throw new IllegalArgumentException("Deck not found");
        return deck;
    }
    public void requireOwned(UUID user, String id) { if (isCustomDeck(id)) owned(user, id); }

    private SaveDeckRequest definition(Deck deck) {
        return new SaveDeckRequest(deck.getName(), Arrays.asList(objectMapper.readValue(deck.getDeckJson(), SaveDeckRequest.DeckEntryInfo[].class)),
                PREFIX + deck.getId(), deck.getFormat(), deck.getSideboardJson() == null ? List.of() :
                Arrays.asList(objectMapper.readValue(deck.getSideboardJson(), SaveDeckRequest.DeckEntryInfo[].class)),
                deck.getCommanderJson() == null ? null : objectMapper.readValue(deck.getCommanderJson(), SaveDeckRequest.DeckEntryInfo.class));
    }
    @Transactional(readOnly = true)
    public SaveDeckRequest load(UUID user, String id) { return definition(owned(user, id)); }
    @Transactional(readOnly = true)
    public List<DeckInfo> getCustomDecksForUser(UUID user) {
        return deckRepository.findByUserId(user).stream().map(deck -> {
            SaveDeckRequest request = definition(deck);
            return new DeckInfo(request.id(), request.name(), request.format(), validate(request));
        }).toList();
    }
    public boolean isCustomDeck(String id) { return id != null && id.startsWith(PREFIX); }
    @Transactional(readOnly = true)
    public DeckDefinition buildDefinition(String id) { return resolve(definition(find(id))); }
    public List<Card> buildCustomDeck(String id) { return new ArrayList<>(buildDefinition(id).mainDeck()); }
    public List<Card> buildCustomSideboard(String id) { return new ArrayList<>(buildDefinition(id).sideboard()); }
}
