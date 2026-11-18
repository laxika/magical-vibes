package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies perpetual type and subtype grants to every matching card the controller owns. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantCardCharacteristicsToOwnedCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect) effect;
        UUID ownerId = entry.getControllerId();
        Set<UUID> modifiedCardIds = new HashSet<>();

        gameData.playerDecks.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, grant, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerHands.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, grant, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerGraveyards.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, grant, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerCommandZones.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, grant, ownerId, zoneOwnerId, cards, modifiedCardIds));

        gameData.playerBattlefields.forEach((controllerId, permanents) -> {
            for (Permanent permanent : permanents) {
                applyToCard(gameData, entry, grant, ownerId, controllerId, permanent.getCard(),
                        permanent.getOriginalCard() == null ? permanent.getCard() : permanent.getOriginalCard(),
                        modifiedCardIds);
            }
        });

        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (ownerId.equals(exiled.ownerId())) {
                    applyToCard(gameData, entry, grant, ownerId, ownerId,
                            exiled.card(), exiled.card(), modifiedCardIds);
                }
            }
        }

        for (StackEntry stackEntry : gameData.stack) {
            applyToCard(gameData, entry, grant, ownerId, null,
                    stackEntry.getCard(), stackEntry.getCard(), modifiedCardIds);
        }
    }

    private void applyToZone(GameData gameData, StackEntry entry,
                             PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, List<Card> cards,
                             Set<UUID> modifiedCardIds) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            applyToCard(gameData, entry, effect, ownerId, zoneOwnerId, card, card, modifiedCardIds);
        }
    }

    /**
     * Records the perpetual change on {@code recorded} when {@code card}, the card as it is now, matches. They differ
     * for a permanent that is a copy: it matches as the card it copies, but the change is made to its own card, which
     * the layered pass reads it from.
     */
    private void applyToCard(GameData gameData, StackEntry entry,
                             PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, Card card, Card recorded,
                             Set<UUID> modifiedCardIds) {
        if (card == null || card.isToken() || modifiedCardIds.contains(card.getId())
                || !isOwnedBy(card, ownerId, zoneOwnerId)
                || !predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), entry.getCard().getId(), gameData, ownerId)) {
            return;
        }

        modifiedCardIds.add(card.getId());
        gameData.perpetualCardTypes.merge(recorded.getId(), Set.copyOf(effect.cardTypes()),
                (existing, added) -> union(existing, added));
        gameData.perpetualCardSubtypes.merge(recorded.getId(), Set.copyOf(effect.subtypes()),
                (existing, added) -> union(existing, added));
    }

    private boolean isOwnedBy(Card card, UUID ownerId, UUID zoneOwnerId) {
        return ownerId.equals(card.getOwnerId())
                || (card.getOwnerId() == null && ownerId.equals(zoneOwnerId));
    }

    private static <T> Set<T> union(Set<T> existing, Set<T> added) {
        Set<T> merged = new HashSet<>(existing);
        merged.addAll(added);
        return Set.copyOf(merged);
    }
}
