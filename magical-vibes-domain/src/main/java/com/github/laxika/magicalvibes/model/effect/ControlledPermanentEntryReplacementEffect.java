package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for a static effect that changes how matching permanents enter the battlefield.
 */
public interface ControlledPermanentEntryReplacementEffect extends CardEffect {

    PermanentPredicate enteringPermanentPredicate();

    int additionalCounterCount(Permanent enteringPermanent);

    /**
     * Returns a game-state-dependent counter amount, when this replacement effect uses one.
     * Fixed and derived-from-entering-permanent effects return {@code null} and use
     * {@link #additionalCounterCount(Permanent)} instead.
     */
    default DynamicAmount additionalCounterAmount() {
        return null;
    }
}
