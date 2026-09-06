package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCardFromHandOrGraveyardOntoBattlefieldSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName) {
        beginChoice(gameData, playerId, predicate, label, sourceCardId, sourceCardName, null);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            UUID sourceCardId, String sourceCardName, CounterType enterWithCounter) {
        List<UUID> validCardIds = new ArrayList<>();
        addMatchingCardIds(validCardIds, gameData.playerHands.get(playerId), predicate,
                sourceCardId, gameData, playerId);
        addMatchingCardIds(validCardIds, gameData.playerGraveyards.get(playerId), predicate,
                sourceCardId, gameData, playerId);
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutCardFromHandOrGraveyardChoice(
                        playerId, validCardIds, label, sourceCardName, enterWithCounter));
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName) {
        applyChoice(gameData, playerId, chosenCardId, sourceCardName, null);
    }

    public void applyChoice(GameData gameData, UUID playerId, UUID chosenCardId, String sourceCardName,
                            CounterType enterWithCounter) {
        Card chosen = removeCard(gameData.playerHands.get(playerId), chosenCardId);
        String zone = "hand";
        if (chosen == null) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            chosen = removeCard(graveyard, chosenCardId);
            zone = "graveyard";
            if (chosen != null) {
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId, chosen);
            }
        }
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer in its hand or graveyard");
        }

        Permanent permanent = new Permanent(chosen);
        if (enterWithCounter != null) {
            permanent.setCounterCount(enterWithCounter, 1);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of());
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " puts "
                + chosen.getName() + " from their " + zone + " onto the battlefield ("
                + sourceCardName + ")."));
    }

    private void addMatchingCardIds(List<UUID> destination, List<Card> cards, CardPredicate predicate,
                                    UUID sourceCardId, GameData gameData, UUID playerId) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId,
                    gameData, playerId)) {
                destination.add(card.getId());
            }
        }
    }

    private Card removeCard(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.getId().equals(cardId)) {
                cards.remove(i);
                return card;
            }
        }
        return null;
    }
}
