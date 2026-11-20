package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Stamps a one-shot combat requirement on a creature for the rest of the turn: a floating effect on the creature
 * until end of turn ({@link MustAttackIfAbleEffect}, {@link MustBlockEachCombatEffect},
 * {@link MustBeBlockedIfAbleEffect} or {@link MustBeBlockedByAllCreaturesEffect}).
 *
 * <p>These are the one-shot counterparts of the static abilities
 * ({@link MustBeBlockedIfAbleEffect}, {@link MustBeBlockedByAllCreaturesEffect}, {@link MustAttackEffect}):
 * same requirement, but granted temporarily by a resolving spell or ability rather than printed on the
 * creature.
 *
 * <p>Per CR 508.1d (attacking) and CR 509.1c (blocking), the affected player is not required to pay any
 * cost to obey the requirement.
 *
 * @param requirement which requirement to impose; see {@link CombatRequirement}
 * @param subject       whom the requirement lands on; {@link Subjects#target()} targets a creature,
 *                    {@link Subjects#target()} applies it to every target in one multi-target group,
 *                    {@link Subjects#source()} applies to the source permanent,
 *                    {@link Subjects#attachedCreature()} applies to the source Aura's attached
 *                    creature, and {@link Subjects#creaturesYouControl()} applies to every creature
 *                    the controller controls
 */
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement, PermanentSubject subject,
                                                  boolean allowPermanentTarget)
        implements CardEffect {

    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement, PermanentSubject subject) {
        this(requirement, subject, false);
    }

    /**
     * The overwhelmingly common shape: the requirement lands on the spell's single target.
     */
    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement) {
        this(requirement, Subjects.target(), false);
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
        return new SetCombatRequirementThisTurnEffect(requirement, Subjects.target(), true);
    }

    /**
     * Only creatures can carry any of these requirements. {@code MUST_BLOCK} additionally carries the
     * explicit predicate, which preserves the creature restriction on targeted-trigger candidates.
     * {@link Subjects#creaturesYouControl()} sweeps the controller's creatures without targeting
     * (Joraga Invocation), so it declares no target at all.
     */
    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Source) {
            return new TargetSpec(null, false, null, true, 1);
        }
        if (Subjects.attachedCreature().equals(subject)) {
            return new TargetSpec(null, false, null, true, 1);
        }
        if (!(subject instanceof PermanentSubject.Target)) {
            return TargetSpec.NONE;
        }

        if (allowPermanentTarget) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }

        return requirement == CombatRequirement.MUST_BLOCK
                ? TargetSpec.benign(TargetPredicates.creature(), new PermanentIsCreaturePredicate())
                : TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return Subjects.attachedCreature().equals(subject);
    }
}
