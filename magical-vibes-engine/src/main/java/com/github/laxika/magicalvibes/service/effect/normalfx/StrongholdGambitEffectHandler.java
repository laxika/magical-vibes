package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StrongholdGambitEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrongholdGambitEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StrongholdGambitEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextChoice(gameData, apnapPlayers(gameData), Map.of(), entry.getCard().getName());
    }

    /** Completes one mandatory hand choice and advances to the next player. */
    public void completeChoice(GameData gameData,
                               PendingInteraction.StrongholdGambitCardChoice choice, int cardIndex) {
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        List<Card> hand = gameData.playerHands.get(choice.playerId());
        if (hand == null || cardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        Card selectedCard = hand.get(cardIndex);
        gameData.interaction.clearAwaitingInput();

        Map<UUID, UUID> chosenCardIds = new LinkedHashMap<>(choice.chosenCardIds());
        chosenCardIds.put(choice.playerId(), selectedCard.getId());
        beginNextChoice(gameData, choice.remainingPlayerIds(), chosenCardIds, choice.sourceName());
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private boolean beginNextChoice(GameData gameData, List<UUID> playerIds,
                                    Map<UUID, UUID> chosenCardIds, String sourceName) {
        for (int i = 0; i < playerIds.size(); i++) {
            UUID playerId = playerIds.get(i);
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty()) {
                continue;
            }

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.StrongholdGambitCardChoice(
                    playerId,
                    IntStream.range(0, hand.size()).boxed().toList(),
                    playerIds.subList(i + 1, playerIds.size()),
                    chosenCardIds,
                    sourceName));
            log.info("Game {} - Awaiting {} to choose a card for {}",
                    gameData.id, gameData.playerIdToName.get(playerId), sourceName);
            return true;
        }

        resolveChosenCards(gameData, chosenCardIds, sourceName);
        return false;
    }

    private void resolveChosenCards(GameData gameData, Map<UUID, UUID> chosenCardIds, String sourceName) {
        List<ChosenCard> chosenCards = new ArrayList<>();
        for (Map.Entry<UUID, UUID> choice : chosenCardIds.entrySet()) {
            Card card = findCard(gameData.playerHands.get(choice.getKey()), choice.getValue());
            if (card != null) {
                chosenCards.add(new ChosenCard(choice.getKey(), card));
                cardRevealService.revealToAllPlayers(
                        gameData, choice.getKey(), GameEventFact.RevealZone.HAND, List.of(card));
            }
        }

        if (!chosenCards.isEmpty()) {
            GameLog.Builder revealLog = GameLog.builder().text("Players reveal their chosen cards: ");
            for (int i = 0; i < chosenCards.size(); i++) {
                if (i > 0) {
                    revealLog.text(", ");
                }
                ChosenCard chosen = chosenCards.get(i);
                revealLog.text(gameData.playerIdToName.get(chosen.chooserId()) + " reveals ")
                        .card(chosen.card());
            }
            gameLogService.append(gameData, revealLog.text(".").build());
        }

        List<ChosenCard> creatures = chosenCards.stream()
                .filter(chosen -> chosen.card().hasType(CardType.CREATURE))
                .toList();
        if (creatures.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " reveals no creature cards."));
            return;
        }

        int lowestManaValue = creatures.stream()
                .mapToInt(chosen -> chosen.card().getManaValue())
                .min()
                .orElseThrow();
        List<ChosenCard> winners = creatures.stream()
                .filter(chosen -> chosen.card().getManaValue() == lowestManaValue)
                .toList();

        List<ChosenCard> cardsToPut = new ArrayList<>();
        for (ChosenCard winner : winners) {
            List<Card> hand = gameData.playerHands.get(winner.chooserId());
            if (hand != null && hand.removeIf(card -> card.getId().equals(winner.card().getId()))) {
                cardsToPut.add(winner);
            }
        }

        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> alreadyEntered = new ArrayList<>();
        for (ChosenCard winner : cardsToPut) {
            UUID controllerId = winner.card().getOwnerId() != null
                    ? winner.card().getOwnerId()
                    : winner.chooserId();
            if (!gameData.playerBattlefields.containsKey(controllerId)) {
                controllerId = winner.chooserId();
            }

            Permanent permanent = new Permanent(winner.card());
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, List.copyOf(alreadyEntered));
            alreadyEntered.add(permanent);
        }
    }

    private static Card findCard(List<Card> hand, UUID cardId) {
        if (hand == null || cardId == null) {
            return null;
        }
        return hand.stream().filter(card -> cardId.equals(card.getId())).findFirst().orElse(null);
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

    private record ChosenCard(UUID chooserId, Card card) {
    }
}
