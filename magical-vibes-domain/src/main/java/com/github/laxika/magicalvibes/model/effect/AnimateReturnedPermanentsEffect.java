package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Applies a permanent animation to permanents created by the preceding graveyard return.
 *
 * <p>The returned card IDs remain on the stack entry, while the battlefield permanents receive
 * new permanent IDs, so the normal permanent-target animation cannot be used directly.</p>
 */
public record AnimateReturnedPermanentsEffect(AnimatePermanentsEffect animation) implements CardEffect {

    public AnimateReturnedPermanentsEffect {
        Objects.requireNonNull(animation);
        if (animation.scope() != GrantScope.TARGET || animation.duration() != EffectDuration.PERMANENT) {
            throw new IllegalArgumentException("Returned permanent animation must target permanently");
        }
    }
}
