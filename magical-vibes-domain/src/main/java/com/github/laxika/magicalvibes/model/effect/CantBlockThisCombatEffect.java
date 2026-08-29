package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect that prevents a target creature, or the source creature, from blocking for the
 * current combat.
 *
 * @param selfTargeting whether the source permanent is affected instead of a target
 */
public record CantBlockThisCombatEffect(boolean selfTargeting) implements CardEffect {

    /** Creates the targeted form used by activated abilities such as Forgestoker Dragon. */
    public CantBlockThisCombatEffect() {
        this(false);
    }

    /** Creates the non-targeted form that affects the source permanent. */
    public static CantBlockThisCombatEffect self() {
        return new CantBlockThisCombatEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.creature());
    }
}
