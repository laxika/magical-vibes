package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Applies an existing permanent animation to the permanents created by the preceding targeted
 * graveyard return effect.
 */
public record AnimateReturnedPermanentsEffect(AnimatePermanentsEffect animation)
        implements CardEffect {

    public AnimateReturnedPermanentsEffect {
        Objects.requireNonNull(animation);
    }
}
