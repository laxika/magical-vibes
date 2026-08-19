package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the hand choices and discards for Noxious Vapors. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffectHandler
        implements NormalEffectHandlerBean {

    private static final List<CardColor> COLORS = List.of(
            CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            cardRevealService.revealHandToAllPlayers(gameData, playerId);
        }
        step(gameData, apnapPlayers(gameData), 0, 0, List.of(), Map.of(),
                entry.getControllerId(), sourceName);
    }

    /** Completes one color choice and advances through the remaining colors and players. */
    public void completeChoice(GameData gameData,
                               PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice choice,
                               int cardIndex) {
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        List<Card> hand = gameData.playerHands.get(choice.playerId());
        if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        Card selected = hand.get(cardIndex);
        if (selected.hasType(CardType.LAND)
                || !gameQueryService.getEffectiveCardColors(gameData, selected)
                .contains(COLORS.get(choice.colorIndex()))) {
            throw new IllegalStateException("Selected card is not a valid card of the chosen color");
        }

        List<UUID> chosenForPlayer = new ArrayList<>(choice.chosenCardIds());
        if (!chosenForPlayer.contains(selected.getId())) {
            chosenForPlayer.add(selected.getId());
        }

        gameData.interaction.clearAwaitingInput();
        step(gameData, choice.playerIds(), choice.playerIndex(), choice.colorIndex() + 1,
                chosenForPlayer, choice.chosenCardIdsByPlayer(), choice.controllerId(), choice.sourceName());
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex, int colorIndex,
                      List<UUID> chosenForPlayer,
                      Map<UUID, List<UUID>> chosenCardIdsByPlayer,
                      UUID controllerId, String sourceName) {
        Map<UUID, List<UUID>> completedChoices = new LinkedHashMap<>(chosenCardIdsByPlayer);
        List<UUID> currentChoices = new ArrayList<>(chosenForPlayer);
        int currentPlayerIndex = playerIndex;
        int currentColorIndex = colorIndex;

        while (currentPlayerIndex < playerIds.size()) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());

            while (currentColorIndex < COLORS.size()) {
                CardColor color = COLORS.get(currentColorIndex);
                List<Integer> candidates = candidates(gameData, hand, color);
                int promptedColorIndex = currentColorIndex++;
                if (candidates.isEmpty()) {
                    continue;
                }

                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.EachPlayerChoosesOneCardOfEachColorChoice(
                                playerId, candidates, playerIds, currentPlayerIndex, promptedColorIndex,
                                currentChoices, completedChoices, controllerId, sourceName));
                log.info("Game {} - Awaiting {} to choose a {} card for {}", gameData.id,
                        gameData.playerIdToName.get(playerId), color.name().toLowerCase(), sourceName);
                return;
            }

            completedChoices.put(playerId, List.copyOf(currentChoices));
            currentPlayerIndex++;
            currentColorIndex = 0;
            currentChoices = new ArrayList<>();
        }

        discardUnchosenCards(gameData, playerIds, completedChoices, controllerId, sourceName);
    }

    private List<Integer> candidates(GameData gameData, List<Card> hand, CardColor color) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.LAND)
                    && gameQueryService.getEffectiveCardColors(gameData, card).contains(color)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void discardUnchosenCards(GameData gameData, List<UUID> playerIds,
                                      Map<UUID, List<UUID>> chosenCardIdsByPlayer,
                                      UUID controllerId, String sourceName) {
        for (UUID playerId : playerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                continue;
            }

            Set<UUID> chosenIds = new java.util.HashSet<>(
                    chosenCardIdsByPlayer.getOrDefault(playerId, List.of()));
            List<Card> discarded = hand.stream()
                    .filter(card -> !card.hasType(CardType.LAND) && !chosenIds.contains(card.getId()))
                    .toList();
            if (discarded.isEmpty()) {
                continue;
            }

            hand.removeAll(discarded);
            gameData.discardCausedByOpponent = !playerId.equals(controllerId);
            triggerCollectionService.beginDiscardEvent(gameData, playerId);
            for (Card card : discarded) {
                graveyardService.discardCard(gameData, playerId, card);
                triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
            }
            triggerCollectionService.finishDiscardEvent(gameData);

            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " discards " + discarded.size()
                    + " nonland card" + (discarded.size() == 1 ? "" : "s") + " (" + sourceName + ")."));
            log.info("Game {} - {} discards {} nonland card(s) for {}", gameData.id, playerName,
                    discarded.size(), sourceName);
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }
}
