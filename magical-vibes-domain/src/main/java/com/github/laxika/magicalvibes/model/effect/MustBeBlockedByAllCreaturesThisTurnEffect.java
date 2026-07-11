package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, all creatures able to block the targeted creature this turn do so (Lure-style).
 * Sets a transient flag on the Permanent that is cleared at end of turn via {@code resetModifiers()}.
 * <p>
 * Unlike {@link MustBeBlockedByAllCreaturesEffect} which is a static ability on the creature itself,
 * this effect is a one-shot that grants the restriction temporarily (e.g. Alluring Scent).
 */
public record MustBeBlockedByAllCreaturesThisTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
