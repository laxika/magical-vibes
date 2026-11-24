package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Goads creatures (CR 701.15a): until the ability controller's next turn, or for {@code duration}. The creature is the
 * effect's target when {@code goaded} is null; otherwise the permanents a subject that doesn't target names as the
 * ability resolves: "goad it" ({@link #ofTriggeringPermanent()}), "goad each creature that had a counter put on it this
 * way", "the tokens are goaded for the rest of the game" ({@code createdThisResolution()} with
 * {@link Duration#PERMANENT}), "goad each creature your opponents control that's enchanted by an Aura you control" (a
 * set, locked as it resolves, CR 611.2c). One that has left the battlefield or isn't a creature is not goaded.
 *
 * <p>A {@link StepKind#GOAD} step: it records the creatures it goaded and their controllers ("for each creature goaded
 * this way"). A creature the same player goads again is recorded too, though no requirement is added (CR 701.15d: the
 * attack rules count one per goading player).
 *
 * <p>The record itself is stored in each resulting floating effect, so readers of the goaded designation see it through
 * {@link GoadStatusEffect} and {@link CombatAttackRequirementEffect}. "For as long as this remains on the battlefield"
 * ({@link Duration#WHILE_SOURCE_REMAINS}) binds each floating effect to the source permanent.
 *
 * @param targetRestriction the target's restriction; only for a target
 * @param goaded            null for the target, else a subject that declares no target
 * @param duration          how long they are goaded: {@link Duration#UNTIL_YOUR_NEXT_TURN} (CR 701.15a) unless the
 *                          text says otherwise
 */
public record GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction, PermanentSubject goaded,
                                                    Duration duration)
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    public GoadTargetCreatureUntilNextTurnEffect {
        Objects.requireNonNull(duration, "duration");
        if (goaded != null && goaded.declaresTarget()) {
            throw new IllegalArgumentException("A goaded subject that targets is the effect's target: " + goaded);
        }
        if (goaded != null && targetRestriction != null) {
            throw new IllegalArgumentException("A goad of a permanent it doesn't target has no target restriction");
        }
    }

    public GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction, PermanentSubject goaded) {
        this(targetRestriction, goaded, Duration.UNTIL_YOUR_NEXT_TURN);
    }

    public GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction) {
        this(targetRestriction, null);
    }

    public GoadTargetCreatureUntilNextTurnEffect() {
        this(null, null);
    }

    /** "Goad [these creatures]" for {@code duration}: the permanents a subject that doesn't target names. */
    public static GoadTargetCreatureUntilNextTurnEffect of(PermanentSubject goaded, Duration duration) {
        return new GoadTargetCreatureUntilNextTurnEffect(null, Objects.requireNonNull(goaded, "goaded"), duration);
    }

    /** "As long as this remains on the battlefield, that creature is also goaded": the target. */
    public static GoadTargetCreatureUntilNextTurnEffect targetWhileSourceRemains() {
        return new GoadTargetCreatureUntilNextTurnEffect(null, null, Duration.WHILE_SOURCE_REMAINS);
    }

    /** "Goad each creature that had a counter put on it this way": the latest put-counters step's permanents. */
    public static GoadTargetCreatureUntilNextTurnEffect ofCreaturesGivenCountersThisWay() {
        return new GoadTargetCreatureUntilNextTurnEffect(null, Subjects.thisWay(StepKind.PUT_COUNTERS));
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
