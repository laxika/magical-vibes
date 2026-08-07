package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the affected creature(s) must be blocked this turn if able.
 * Sets a transient flag on the Permanent that is cleared at end of turn
 * via {@code resetModifiers()}.
 * <p>
 * Unlike {@link MustBeBlockedIfAbleEffect} which is a static ability on the creature itself,
 * this effect is a one-shot that grants the restriction temporarily (e.g. Emergent Growth).
 * <p>
 * {@link GrantScope#TARGET} affects the spell's target; {@link GrantScope#OWN_CREATURES}
 * affects every creature the controller controls without targeting (Joraga Invocation).
 */
public record MustBeBlockedIfAbleThisTurnEffect(GrantScope scope) implements CardEffect {

    public MustBeBlockedIfAbleThisTurnEffect() {
        this(GrantScope.TARGET);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
