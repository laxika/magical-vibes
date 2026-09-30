package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForMatchingOwnedCardsEffect;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyReduceCostForMatchingOwnedCardsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyReduceCostForMatchingOwnedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reduction = (PerpetuallyReduceCostForMatchingOwnedCardsEffect) effect;
        UUID ownerId = entry.getControllerId();
        Set<UUID> modifiedCardIds = new HashSet<>();

        gameData.playerDecks.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, reduction, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerHands.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, reduction, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerGraveyards.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, reduction, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerCommandZones.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, reduction, ownerId, zoneOwnerId, cards, modifiedCardIds));

        gameData.playerBattlefields.forEach((controllerId, permanents) -> {
            for (var permanent : permanents) {
                applyToCard(gameData, entry, reduction, ownerId, controllerId,
                        permanent.getCard(), modifiedCardIds);
            }
        });

        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (ownerId.equals(exiled.ownerId())) {
                    applyToCard(gameData, entry, reduction, ownerId, ownerId,
                            exiled.card(), modifiedCardIds);
                }
            }
        }

        for (StackEntry stackEntry : gameData.stack) {
            applyToCard(gameData, entry, reduction, ownerId, null,
                    stackEntry.getCard(), modifiedCardIds);
        }
    }

    private void applyToZone(GameData gameData, StackEntry entry,
                             PerpetuallyReduceCostForMatchingOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, List<Card> cards,
                             Set<UUID> modifiedCardIds) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            applyToCard(gameData, entry, effect, ownerId, zoneOwnerId, card, modifiedCardIds);
        }
    }

    private void applyToCard(GameData gameData, StackEntry entry,
                             PerpetuallyReduceCostForMatchingOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, Card card,
                             Set<UUID> modifiedCardIds) {
        if (card == null || modifiedCardIds.contains(card.getId())
                || !isOwnedBy(card, ownerId, zoneOwnerId)
                || !predicateEvaluationService.matchesCardPredicate(
                card, effect.filter(), entry.getCard().getId(), gameData, ownerId)) {
            return;
        }

        modifiedCardIds.add(card.getId());
        PerpetualCardCastCostSupport.remember(gameData, card, effect.amount());
    }

    private boolean isOwnedBy(Card card, UUID ownerId, UUID zoneOwnerId) {
        return ownerId.equals(card.getOwnerId())
                || (card.getOwnerId() == null && ownerId.equals(zoneOwnerId));
    }
}
