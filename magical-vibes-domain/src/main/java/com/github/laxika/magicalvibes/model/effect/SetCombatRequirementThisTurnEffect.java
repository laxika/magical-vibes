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
 * @param scope       whom the requirement lands on; {@link GrantScope#TARGET} targets a creature,
 *                    {@link GrantScope#SELF} applies to the source permanent, and
 *                    {@link GrantScope#ALL_OWN_CREATURES} applies to every creature the controller controls
 */
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement, GrantScope scope,
                                                  boolean allowPermanentTarget) implements CardEffect {

    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement, GrantScope scope) {
        this(requirement, scope, false);
    }

    /**
     * The overwhelmingly common shape: the requirement lands on the spell's single target.
     */
    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement) {
        this(requirement, GrantScope.TARGET, false);
    }

    /**
     * Variant for effects that first turn a targeted permanent into a creature while resolving.
     */
    public static SetCombatRequirementThisTurnEffect forAnimatedPermanent(CombatRequirement requirement) {
        return targetPermanent(requirement);
    }

    /**
     * Targets a permanent that an earlier effect in the same resolution turns into a creature.
     * The card's own target filter must still restrict that permanent to the card's legal choices.
     */
    public static SetCombatRequirementThisTurnEffect targetPermanent(CombatRequirement requirement) {
        return new SetCombatRequirementThisTurnEffect(requirement, GrantScope.TARGET, true);
    }

    /**
     * Only creatures can carry any of these requirements. {@code MUST_BLOCK} additionally carries the
     * explicit predicate, which preserves the creature restriction on targeted-trigger candidates.
     * {@link GrantScope#ALL_OWN_CREATURES} sweeps the controller's creatures without targeting
     * (Joraga Invocation), so it declares no target at all.
     */
    @Override
    public TargetSpec targetSpec() {
        if (scope == GrantScope.SELF) {
            return new TargetSpec(null, false, null, true, 1);
        }
        if (scope != GrantScope.TARGET) {
            return TargetSpec.NONE;
        }

        if (allowPermanentTarget) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }

        return requirement == CombatRequirement.MUST_BLOCK
                ? TargetSpec.benign(TargetPredicates.creature(), new PermanentIsCreaturePredicate())
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
