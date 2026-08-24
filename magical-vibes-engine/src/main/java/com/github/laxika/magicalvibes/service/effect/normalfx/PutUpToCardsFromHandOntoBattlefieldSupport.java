package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutUpToCardsFromHandOntoBattlefieldSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            int maxCount, UUID sourceCardId, String sourceCardName, boolean tapped) {
        if (maxCount <= 0) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        List<UUID> validCardIds = new ArrayList<>();
        if (hand != null) {
            for (Card card : hand) {
                if (predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId,
                        gameData, playerId)) {
                    validCardIds.add(card.getId());
                }
            }
        }

        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice(
                        playerId, validCardIds, Math.min(maxCount, validCardIds.size()), sourceCardName, tapped));
        log.info("Game {} - Awaiting {} to put up to {} {} cards from hand onto the battlefield",
                gameData.id, gameData.playerIdToName.get(playerId),
                Math.min(maxCount, validCardIds.size()), label);
    }

    public void applyPutChoice(GameData gameData, UUID playerId, List<UUID> chosenCardIds,
                               String sourceCardName, boolean tapped) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || chosenCardIds.isEmpty()) {
            return;
        }

        Set<UUID> chosenIds = new HashSet<>(chosenCardIds);
        List<Card> chosenCards = new ArrayList<>();
        hand.removeIf(card -> {
            if (chosenIds.contains(card.getId())) {
                chosenCards.add(card);
                return true;
            }
            return false;
        });

        Set<com.github.laxika.magicalvibes.model.CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> alreadyEntered = new ArrayList<>();
        for (Card card : chosenCards) {
            Permanent permanent = new Permanent(card);
            if (tapped) {
                permanent.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                    enterTappedTypes, List.copyOf(alreadyEntered));
            alreadyEntered.add(permanent);
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " puts "
                + chosenCards.size() + (chosenCards.size() == 1 ? " card" : " cards")
                + " from their hand onto the battlefield (" + sourceCardName + ")."));
    }
}
