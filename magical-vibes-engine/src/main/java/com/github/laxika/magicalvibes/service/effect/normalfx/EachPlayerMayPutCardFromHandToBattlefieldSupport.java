package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Coordinates Show and Tell-style choices in active-player order and places all chosen cards onto
 * the battlefield as one simultaneous batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCardFromHandToBattlefieldSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    /** Begins the next eligible player's optional choice, or places the accumulated choices. */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   List<UUID> chosenCardIds,
                                   EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                   String cardName) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<UUID> validCardIds = validCardIds(gameData, playerId, effect);
            if (validCardIds.isEmpty()) {
                continue;
            }

            List<UUID> remainingPlayerIds = new ArrayList<>(orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.EachPlayerMayPutCardFromHandChoice(
                    playerId, validCardIds, remainingPlayerIds, chosenCardIds, effect.predicate(),
                    effect.label(), cardName));
            log.info("Game {} - Awaiting {} to choose an {} to put onto the battlefield ({})",
                    gameData.id, gameData.playerIdToName.get(playerId), effect.label(), cardName);
            return true;
        }

        putChosenCardsOntoBattlefield(gameData, chosenCardIds, effect, cardName);
        return false;
    }

    private List<UUID> validCardIds(GameData gameData, UUID playerId,
                                    EachPlayerMayPutCardFromHandToBattlefieldEffect effect) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.predicate(), card.getId(), gameData, playerId))
                .map(Card::getId)
                .toList();
    }

    private void putChosenCardsOntoBattlefield(GameData gameData, List<UUID> chosenCardIds,
                                                EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                                String cardName) {
        if (chosenCardIds.isEmpty()) {
            return;
        }

        List<ChosenCard> chosenCards = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> hand = gameData.playerHands.get(playerId);
                if (hand == null) {
                    continue;
                }
                Card chosenCard = hand.stream().filter(card -> card.getId().equals(cardId)).findFirst().orElse(null);
                if (chosenCard != null) {
                    hand.remove(chosenCard);
                    chosenCards.add(new ChosenCard(playerId, chosenCard));
                    break;
                }
            }
        }

        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> alreadyEntered = new ArrayList<>();
        for (ChosenCard chosenCard : chosenCards) {
            Permanent permanent = new Permanent(chosenCard.card());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, chosenCard.playerId(), permanent,
                    enterTappedTypes, List.copyOf(alreadyEntered));
            alreadyEntered.add(permanent);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(chosenCard.playerId()) + " puts ")
                    .card(chosenCard.card())
                    .text(" onto the battlefield (" + cardName + ").")
                    .build());
        }
        log.info("Game {} - Put {} chosen {} card(s) onto the battlefield",
                gameData.id, chosenCards.size(), effect.label());
    }

    private record ChosenCard(UUID playerId, Card card) {
    }
}
