package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** State and completion support for Niv-Mizzet's color-pair reveal. */
@Component
@RequiredArgsConstructor
public class NivMizzetRevealSupport {

    private static final List<CardColor> COLOR_ORDER = List.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void begin(GameData gameData, UUID controllerId, List<Card> revealedCards) {
        List<UUID> validCardIds = revealedCards.stream()
                .filter(card -> colorPair(gameData, card) != null)
                .map(Card::getId)
                .toList();
        int requiredPairCount = (int) revealedCards.stream()
                .map(card -> colorPair(gameData, card))
                .filter(pair -> pair != null)
                .distinct()
                .count();

        if (requiredPairCount == 0) {
            finish(gameData, controllerId, revealedCards, List.of());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.NivMizzetColorPairChoice(
                controllerId,
                revealedCards,
                validCardIds,
                requiredPairCount,
                "Choose one card for each color pair represented among the revealed cards."));
    }

    public void handleChoice(GameData gameData, PendingInteraction.NivMizzetColorPairChoice choice,
                             List<UUID> cardIds) {
        if (cardIds == null || cardIds.size() != choice.requiredPairCount()) {
            throw new IllegalStateException("Must choose one card for each represented color pair");
        }
        if (new HashSet<>(cardIds).size() != cardIds.size()
                || !choice.validCardIds().containsAll(cardIds)) {
            throw new IllegalStateException("Invalid Niv-Mizzet card selection");
        }

        Map<UUID, Card> cardsById = new HashMap<>();
        for (Card card : choice.revealedCards()) {
            cardsById.put(card.getId(), card);
        }
        Set<ColorPair> selectedPairs = new HashSet<>();
        for (UUID cardId : cardIds) {
            ColorPair pair = colorPair(gameData, cardsById.get(cardId));
            if (pair == null || !selectedPairs.add(pair)) {
                throw new IllegalStateException("Choose no more than one card for each color pair");
            }
        }
        if (selectedPairs.size() != choice.requiredPairCount()) {
            throw new IllegalStateException("Must choose one card for each represented color pair");
        }

        gameData.interaction.clearAwaitingInput();
        finish(gameData, choice.playerId(), choice.revealedCards(), cardIds);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void finish(GameData gameData, UUID controllerId, List<Card> revealedCards,
                        List<UUID> selectedCardIds) {
        Set<UUID> selected = Set.copyOf(selectedCardIds);
        List<Card> selectedCards = new ArrayList<>();
        List<Card> remainingCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (selected.contains(card.getId())) {
                selectedCards.add(card);
            } else {
                remainingCards.add(card);
            }
        }

        for (Card card : selectedCards) {
            gameData.addCardToHand(controllerId, card);
        }
        Collections.shuffle(remainingCards);
        gameData.playerDecks.get(controllerId).addAll(remainingCards);

        if (!selectedCards.isEmpty()) {
            GameLog.Builder handLog = GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " puts ");
            for (int i = 0; i < selectedCards.size(); i++) {
                if (i > 0) {
                    handLog.text(", ");
                }
                handLog.card(selectedCards.get(i));
            }
            gameLogService.append(gameData,
                    handLog.text(" into their hand; the rest go to the bottom of the library in a random order.")
                            .build());
        }
    }

    private ColorPair colorPair(GameData gameData, Card card) {
        if (card == null) {
            return null;
        }
        Set<CardColor> colors = gameQueryService.getEffectiveCardColors(gameData, card);
        if (colors.size() != 2) {
            return null;
        }
        List<CardColor> ordered = COLOR_ORDER.stream().filter(colors::contains).toList();
        return new ColorPair(ordered.get(0), ordered.get(1));
    }

    private record ColorPair(CardColor first, CardColor second) {
    }
}
