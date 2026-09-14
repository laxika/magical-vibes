package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
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
        counters.forEach((counterType, count) -> permanent.setCounterCount(
                counterType, permanent.getCounterCount(counterType) + count));
    }

    private static boolean hasPersistenceAbility(Card card) {
        return card != null && card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreserveCountersOnZoneChangeEffect.class::isInstance);
    }
}
