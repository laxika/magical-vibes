package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldsWithinWorldsSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    public List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   List<UUID> exiledCardIds,
                                   Map<UUID, List<UUID>> chosenCardIdsByPlayer,
                                   String cardName) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<UUID> validCardIds = creatureCardIdsInHand(gameData, playerId);
            if (validCardIds.isEmpty()) {
                continue;
            }
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.WorldsWithinWorldsChoice(
                    playerId, validCardIds, orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()),
                    exiledCardIds, chosenCardIdsByPlayer, cardName));
            log.info("Game {} - Awaiting {} to choose creature cards for {}", gameData.id,
                    gameData.playerIdToName.get(playerId), cardName);
            return true;
        }
        return false;
    }

    public List<UUID> creatureCardIdsInHand(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .toList();
    }

    public void finish(GameData gameData, List<UUID> exiledCardIds,
                       Map<UUID, List<UUID>> chosenCardIdsByPlayer, String cardName) {
        List<ChosenCard> chosenCards = removeChosenCardsFromHands(gameData, chosenCardIdsByPlayer);
        Set<com.github.laxika.magicalvibes.model.CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (ChosenCard chosenCard : chosenCards) {
            Permanent permanent = new Permanent(chosenCard.card());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, chosenCard.controllerId(),
                    permanent, enterTappedTypes, List.copyOf(simultaneouslyEntered));
            simultaneouslyEntered.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(chosenCard.card(),
                    gameData.playerIdToName.get(chosenCard.controllerId())));
        }

        for (UUID exiledCardId : exiledCardIds) {
            ExiledCardEntry exiled = gameData.findExiledCard(exiledCardId);
            if (exiled == null || !gameData.removeFromExile(exiledCardId)) {
                continue;
            }
            gameData.addCardToHand(exiled.ownerId(), exiled.card());
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(exiled.ownerId()) + " puts ", exiled.card(),
                    " from exile into their hand (" + cardName + ")."));
        }
    }

    private List<ChosenCard> removeChosenCardsFromHands(GameData gameData,
                                                         Map<UUID, List<UUID>> chosenCardIdsByPlayer) {
        List<ChosenCard> chosenCards = new ArrayList<>();
        for (Map.Entry<UUID, List<UUID>> entry : chosenCardIdsByPlayer.entrySet()) {
            List<Card> hand = gameData.playerHands.get(entry.getKey());
            if (hand == null || entry.getValue().isEmpty()) {
                continue;
            }
            Set<UUID> chosenIds = new HashSet<>(entry.getValue());
            hand.removeIf(card -> {
                if (!chosenIds.contains(card.getId())) {
                    return false;
                }
                chosenCards.add(new ChosenCard(entry.getKey(), card));
                return true;
            });
        }
        return chosenCards;
    }

    private record ChosenCard(UUID controllerId, Card card) {
    }
}
