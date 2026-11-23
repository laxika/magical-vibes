package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Objects;

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
 * @param attacked      with {@link CombatRequirement#MUST_ATTACK}, the player the creature must attack, not that player's
 *                    planeswalkers ({@link #mustAttack}: "attacks you", "attacks target opponent this turn if
 *                    able"), found as the effect resolves (none: nothing is required); {@code null} for any player
 *                    or planeswalker
 */
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement, PermanentSubject subject,
                                                  boolean allowPermanentTarget, PlayerSubject attacked)
        implements CardEffect {

    public SetCombatRequirementThisTurnEffect {
        Objects.requireNonNull(requirement, "requirement");
        Objects.requireNonNull(subject, "subject");
        if (attacked != null && (requirement != CombatRequirement.MUST_ATTACK || subject.isSet())) {
            throw new IllegalArgumentException("Only an attack requirement on one creature names the player attacked: "
                    + requirement + " " + subject);
        }
    }

    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement, PermanentSubject subject,
                                              boolean allowPermanentTarget) {
        this(requirement, subject, allowPermanentTarget, null);
    }

    public SetCombatRequirementThisTurnEffect(CombatRequirement requirement, PermanentSubject subject) {
        this(requirement, subject, false);
    }

    /**
     * "Target creature attacks [player] this turn if able": {@code subject} must attack the player {@code player}
     * names (Alluring Siren's {@code you()}, Dulcet Sirens' {@code targetPlayer(1)}).
     */
    public static SetCombatRequirementThisTurnEffect mustAttack(PermanentSubject subject, PlayerSubject player) {
        return new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK, subject, false,
                Objects.requireNonNull(player, "player"));
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
     * (Joraga Invocation), so it declares no target at all. A target creature that must attack a target player
     * declares the creature and the player.
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
        if (attacked != null && attacked.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.playerOrPermanent());
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
