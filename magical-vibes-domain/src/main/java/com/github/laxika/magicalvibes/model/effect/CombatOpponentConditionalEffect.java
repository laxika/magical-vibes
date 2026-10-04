package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Fires the wrapped effect from an attached permanent's combat trigger only when at least one
 * relevant combat opponent matches the supplied predicate.
 */
public record CombatOpponentConditionalEffect(PermanentPredicate opponentFilter, CardEffect wrapped)
        implements CombatOpponentFilterEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    @Override
    public boolean hasOptionalTarget() {
        return wrapped.hasOptionalTarget();
    }

    @Override
    public boolean usesEnteringPermanentReference() {
        return wrapped.usesEnteringPermanentReference();
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return wrapped.hasAbilityResolutionCondition();
    }
}
