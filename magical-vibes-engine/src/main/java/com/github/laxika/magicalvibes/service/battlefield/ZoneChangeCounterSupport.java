package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsLoseAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PreserveCountersOnZoneChangeEffect;

import java.util.EnumMap;
import java.util.Map;

/** Handles counters that a card explicitly retains while changing zones. */
public final class ZoneChangeCounterSupport {

    private ZoneChangeCounterSupport() {
    }

    public static void preserve(GameData gameData, Permanent permanent) {
        if (permanent == null || !hasPersistenceAbility(permanent.getCard())) {
            return;
        }
        if (permanent.isFaceDown() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            // A face-down or ability-less permanent doesn't have the ability that retains counters.
            if (permanent.getCard() != null) {
                gameData.countersPreservedAcrossZoneChanges.remove(permanent.getCard().getId());
            }
            return;
        }
        if (permanent.getCounters().isEmpty()) {
            gameData.countersPreservedAcrossZoneChanges.remove(permanent.getCard().getId());
            return;
        }
        gameData.countersPreservedAcrossZoneChanges.put(
                permanent.getCard().getId(), Map.copyOf(new EnumMap<>(permanent.getCounters())));
    }

    public static void restore(GameData gameData, Permanent permanent) {
        if (permanent == null || permanent.getCard() == null) {
            return;
        }
        Card card = permanent.getCard();
        Map<CounterType, Integer> counters = gameData.countersPreservedAcrossZoneChanges.remove(card.getId());
        if (counters == null) {
            return;
        }
        Zone enteredFromZone = permanent.getEnteredFromZone();
        if (!hasPersistenceAbility(card)
                || enteredFromZone == Zone.HAND
                || enteredFromZone == Zone.LIBRARY) {
            return;
        }
        if (enteredFromZone == Zone.GRAVEYARD && gameData.anyPermanentMatches(source -> !source.isFaceDown()
                && !source.isLosesAllAbilitiesUntilEndOfTurn()
                && source.getCard().getStaticEffects().stream()
                .anyMatch(GraveyardCardsLoseAllAbilitiesEffect.class::isInstance))) {
            // Cards in graveyards have no abilities, so the card has no ability to retain its counters.
            return;
        }
        counters.forEach((counterType, count) -> permanent.setCounterCount(
                counterType, permanent.getCounterCount(counterType) + count));
    }

    private static boolean hasPersistenceAbility(Card card) {
        return card != null && card.getStaticEffects().stream()
                .anyMatch(PreserveCountersOnZoneChangeEffect.class::isInstance);
    }
}
