package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Stamps a one-shot combat requirement on the targeted creature for the rest of the turn. The
 * requirement lives as a transient flag on the {@code Permanent} and is cleared at end of turn via
 * {@code resetModifiers()}.
 *
 * <p>These are the one-shot counterparts of the static abilities that read off {@code EffectSlot.STATIC}
 * ({@link MustBeBlockedIfAbleEffect}, {@link MustBeBlockedByAllCreaturesEffect}, {@link MustAttackEffect}):
 * same requirement, but granted temporarily by a resolving spell or ability rather than printed on the
 * creature.
 *
 * <p>Per CR 508.1d (attacking) and CR 509.1c (blocking), the affected player is not required to pay any
 * cost to obey the requirement.
 *
 * @param requirement which requirement to impose; see {@link CombatRequirement}
 */
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement) implements CardEffect {

    /**
     * Only creatures can carry any of these requirements. {@code MUST_BLOCK} additionally carries the
     * explicit predicate, which preserves the creature restriction on targeted-trigger candidates.
     */
    @Override
    public TargetSpec targetSpec() {
        return requirement == CombatRequirement.MUST_BLOCK
                ? TargetSpec.benign(TargetPredicates.creature(), new PermanentIsCreaturePredicate())
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
