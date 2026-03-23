package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the targeted creature must be blocked this turn if able.
 * Sets a transient flag on the Permanent that is cleared at end of turn
 * via {@code resetModifiers()}.
 * <p>
 * Unlike {@link MustBeBlockedIfAbleEffect} which is a static ability on the creature itself,
 * this effect is a one-shot that grants the restriction temporarily (e.g. Emergent Growth).
 */
public record MustBeBlockedIfAbleThisTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
