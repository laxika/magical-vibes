package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostOwnedCardsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostOwnedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PerpetuallyBoostOwnedCardsEffect) effect;
        UUID ownerId = entry.getControllerId();
        Set<UUID> modifiedCardIds = new HashSet<>();

        gameData.playerDecks.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, e, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerHands.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, e, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerGraveyards.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, e, ownerId, zoneOwnerId, cards, modifiedCardIds));
        gameData.playerCommandZones.forEach((zoneOwnerId, cards) -> applyToZone(
                gameData, entry, e, ownerId, zoneOwnerId, cards, modifiedCardIds));

        gameData.playerBattlefields.forEach((controllerId, permanents) -> {
            for (Permanent permanent : permanents) {
                applyToCard(gameData, entry, e, ownerId, controllerId, permanent.getCard(), modifiedCardIds);
            }
        });

        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (ownerId.equals(exiled.ownerId())) {
                    applyToCard(gameData, entry, e, ownerId, ownerId, exiled.card(), modifiedCardIds);
                }
            }
        }

        for (StackEntry stackEntry : gameData.stack) {
            applyToCard(gameData, entry, e, ownerId, null, stackEntry.getCard(), modifiedCardIds);
        }

        if (!modifiedCardIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " perpetually gives matching cards you own "
                            + formatModifier(e.powerBoost(), e.toughnessBoost()) + "."));
        }
    }

    private void applyToZone(GameData gameData, StackEntry entry, PerpetuallyBoostOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, List<Card> cards, Set<UUID> modifiedCardIds) {
        if (cards == null) return;
        for (Card card : cards) {
            applyToCard(gameData, entry, effect, ownerId, zoneOwnerId, card, modifiedCardIds);
        }
    }

    private void applyToCard(GameData gameData, StackEntry entry, PerpetuallyBoostOwnedCardsEffect effect,
                             UUID ownerId, UUID zoneOwnerId, Card card, Set<UUID> modifiedCardIds) {
        if (card == null || modifiedCardIds.contains(card.getId())
                || !isOwnedBy(card, ownerId, zoneOwnerId)
                || !predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), entry.getCard().getId(), gameData, ownerId)) {
            return;
        }

        modifiedCardIds.add(card.getId());
        gameData.perpetualCardPowerToughnessModifiers.merge(
                card.getId(),
                new CardPowerToughnessModifier(effect.powerBoost(), effect.toughnessBoost()),
                (oldValue, newValue) -> oldValue.add(newValue.power(), newValue.toughness()));
    }

    private boolean isOwnedBy(Card card, UUID ownerId, UUID zoneOwnerId) {
        return ownerId.equals(card.getOwnerId())
                || (card.getOwnerId() == null && ownerId.equals(zoneOwnerId));
    }

    private static String formatModifier(int power, int toughness) {
        return String.format("%+d/%+d", power, toughness);
    }
}
