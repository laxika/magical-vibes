package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Goads a creature until the ability controller's next turn (CR 701.15a). The creature is the
 * effect's target when {@code goaded} is null, or, for {@link #ofTriggeringPermanent()}, the
 * permanent the trigger event was about ("goad it"), read from the stack entry's triggering
 * permanent as the ability resolves; that form doesn't target, and does nothing if the permanent
 * has left the battlefield or isn't a creature.
 *
 * <p>The record itself is stored in the resulting floating effect, so readers of the goaded
 * designation see it through {@link GoadStatusEffect} and {@link CombatAttackRequirementEffect}.
 *
 * @param goaded null for the target, or {@link Subjects#triggering()}
 */
public record GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction, PermanentSubject goaded)
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    public GoadTargetCreatureUntilNextTurnEffect {
        if (goaded != null && !(goaded instanceof PermanentSubject.Triggering)) {
            throw new IllegalArgumentException("Goads the target or the triggering permanent: " + goaded);
        }
        if (goaded != null && targetRestriction != null) {
            throw new IllegalArgumentException("A triggering-permanent goad has no target restriction");
        }
    }

    public GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction) {
        this(targetRestriction, null);
    }

    public GoadTargetCreatureUntilNextTurnEffect() {
        this(null, null);
    }

    /** "Goad it": the stack entry's triggering permanent. */
    public static GoadTargetCreatureUntilNextTurnEffect ofTriggeringPermanent() {
        return new GoadTargetCreatureUntilNextTurnEffect(null, Subjects.triggering());
    }

    @Override
    public TargetSpec targetSpec() {
        return goaded == null
                ? TargetSpec.harmful(TargetPredicates.creature(), targetRestriction)
                : TargetSpec.NONE;
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }

    @Override
    public boolean makesGoaded() {
        return true;
    }
}
