package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
     * Returns a game-state-dependent counter amount, when this replacement effect uses one.
     * Fixed and derived-from-entering-permanent effects return {@code null} and use
     * {@link #additionalCounterCount(Permanent)} instead.
     */
    default DynamicAmount additionalCounterAmount() {
        return null;
    }
}
