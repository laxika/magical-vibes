package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a creature that can't have +1/+1 counters put on it.
 *
 * <p>This is intentionally narrower than {@link CantHaveCountersEffect}; it is used for effects
 * such as Blightbeetle that leave other counter types unaffected.</p>
 */
public record CantHavePlusOnePlusOneCountersEffect(GrantScope scope) implements CardEffect {

    public CantHavePlusOnePlusOneCountersEffect() {
        this(GrantScope.SELF);
    }
}
