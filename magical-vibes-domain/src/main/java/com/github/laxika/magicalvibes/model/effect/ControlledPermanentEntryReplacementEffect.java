package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Map;

/**
 * Capability for a static effect that changes how matching permanents enter the battlefield.
 */
public interface ControlledPermanentEntryReplacementEffect extends CardEffect {

    PermanentPredicate enteringPermanentPredicate();

    default CounterType counterType() {
        return CounterType.PLUS_ONE_PLUS_ONE;
    }

    default int additionalCounterCount(Permanent enteringPermanent) {
        return 0;
    }

    default int additionalCounterCount(GameData gameData, Permanent enteringPermanent) {
        return additionalCounterCount(enteringPermanent);
    }

    /**
     * Returns the counters this replacement adds, keyed by counter kind. Most entry replacements
     * add one kind and use the count methods above; effects that copy counter kinds can override
     * this method.
     */
    default Map<CounterType, Integer> additionalCounters(GameData gameData, Permanent source,
                                                          Permanent enteringPermanent) {
        int count = additionalCounterCount(gameData, enteringPermanent);
        CounterType type = counterType();
        return type == null || count <= 0 ? Map.of() : Map.of(type, count);
    }


    /**
     * Returns a game-state-dependent counter amount, when this replacement effect uses one.
     * Fixed and derived-from-entering-permanent effects return {@code null} and use
     * {@link #additionalCounterCount(Permanent)} instead.
     */
    default DynamicAmount additionalCounterAmount() {
        return null;
    }
}
