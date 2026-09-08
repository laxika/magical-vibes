package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Taps the source and every permanent matching {@code filter}, then has the ability controller
 * choose an opponent who gains permanent control of all of them.
 */
public record TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect(
        PermanentPredicate filter) implements ControlStealingEffect {

    public TapAndChooseOpponentGainsControlOfSourceAndMatchingPermanentsEffect {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
