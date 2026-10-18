package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
    private final GraveyardService graveyardService;
    private final AuraAttachmentService auraAttachmentService;

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            int maxCount, UUID sourceCardId, String sourceCardName) {
        beginChoice(gameData, playerId, predicate, label, maxCount, sourceCardId, sourceCardName,
                false, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            int maxCount, UUID sourceCardId, String sourceCardName,
                            boolean includeGraveyard) {
        beginChoice(gameData, playerId, predicate, label, maxCount, sourceCardId, sourceCardName,
                includeGraveyard, false);
    }

    public void beginChoice(GameData gameData, UUID playerId, CardPredicate predicate, String label,
                            int maxCount, UUID sourceCardId, String sourceCardName,
                            boolean includeGraveyard, boolean enterTapped) {
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
        if (includeGraveyard) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId,
                            gameData, playerId)) {
                        validCardIds.add(card.getId());
                    }
                }
            }
        }

        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice(
                        playerId, validCardIds, Math.min(maxCount, validCardIds.size()), sourceCardName,
                        includeGraveyard, enterTapped));
        log.info("Game {} - Awaiting {} to put up to {} {} cards {} onto the battlefield",
                gameData.id, gameData.playerIdToName.get(playerId),
                Math.min(maxCount, validCardIds.size()), label,
                includeGraveyard ? "from hand and/or graveyard" : "from hand");
    }

    public void applyPutChoice(GameData gameData, UUID playerId, List<UUID> chosenCardIds, String sourceCardName) {
        applyPutChoice(gameData, playerId, chosenCardIds, sourceCardName, false, false);
    }

    public void applyPutChoice(GameData gameData, UUID playerId, List<UUID> chosenCardIds, String sourceCardName,
                               boolean includeGraveyard, boolean enterTapped) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> graveyard = includeGraveyard ? gameData.playerGraveyards.get(playerId) : null;
        if ((hand == null && graveyard == null) || chosenCardIds.isEmpty()) {
            return;
        }

        Set<UUID> chosenIds = new HashSet<>(chosenCardIds);
        boolean hasChosenGraveyardCard = graveyard != null && graveyard.stream()
                .anyMatch(card -> chosenIds.contains(card.getId()));
        List<Card> chosenCards = new ArrayList<>();
        if (hasChosenGraveyardCard) {
            graveyardService.beginGraveyardLeaveBatch(gameData);
        }
        try {
            if (hand != null) {
                hand.removeIf(card -> {
                    if (chosenIds.contains(card.getId()) && !staysInHand(gameData, playerId, card)) {
                        chosenCards.add(card);
                        return true;
                    }
                    return false;
                });
            }
            List<Card> chosenGraveyardCards = new ArrayList<>();
            if (graveyard != null) {
                graveyard.removeIf(card -> {
                    if (chosenIds.contains(card.getId())) {
                        chosenCards.add(card);
                        chosenGraveyardCards.add(card);
                        return true;
                    }
                    return false;
                });
            }
            if (!chosenGraveyardCards.isEmpty()) {
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId, chosenGraveyardCards);
            }

            Set<com.github.laxika.magicalvibes.model.CardType> enterTappedTypes =
                    battlefieldEntryService.snapshotEnterTappedTypes(gameData);
            List<Permanent> alreadyEntered = new ArrayList<>();
            for (Card card : chosenCards) {
                Permanent permanent = new Permanent(card);
                if (chosenGraveyardCards.contains(card)) {
                    permanent.setEnteredFromGraveyardOwnerId(playerId);
                }
                if (enterTapped) {
                    permanent.tap();
                }
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                        enterTappedTypes, List.copyOf(alreadyEntered));
                alreadyEntered.add(permanent);
            }
        } finally {
            if (hasChosenGraveyardCard) {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
        }
        for (Card card : chosenCards) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " puts "
                + chosenCards.size() + (chosenCards.size() == 1 ? " card" : " cards")
                + (includeGraveyard ? " from their hand and/or graveyard" : " from their hand")
                + " onto the battlefield (" + sourceCardName + ")."));
    }

    /** An Aura with no legal object to enchant stays in its current zone (CR 303.4g). */
    private boolean staysInHand(GameData gameData, UUID playerId, Card card) {
        if (!card.isAura() || card.isEnchantZone()) {
            return false;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (auraAttachmentService.canEnchant(gameData, card, playerId, permanent)) {
                    return false;
                }
            }
        }
        return !card.isEnchantPlayer() || gameData.orderedPlayerIds.stream()
                .noneMatch(id -> auraAttachmentService.canEnchantPlayer(gameData, card, playerId, id));
    }
}
