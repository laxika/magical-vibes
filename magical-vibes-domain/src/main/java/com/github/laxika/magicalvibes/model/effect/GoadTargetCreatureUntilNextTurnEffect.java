package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * Goads creatures (CR 701.15a): until the ability controller's next turn, or for {@code duration}. The creature is the
 * effect's target when {@code goaded} is null; otherwise the permanents a subject that doesn't target names as the
 * ability resolves: "goad it" ({@link #ofTriggeringPermanent()}), "goad each creature that had a counter put on it this
 * way", "the tokens are goaded for the rest of the game" ({@code createdThisResolution()} with
 * {@link Duration#PERMANENT}), "goad each creature your opponents control that's enchanted by an Aura you control",
 * "goad all creatures you don't control" ({@link #eachCreature}), "goad each creature target player controls"
 * ({@link #creaturesControlledBy}; a set of a target player's permanents declares that player as the harmful target).
 * A set is found once, as the effect resolves (CR 608.2h); creatures that arrive later aren't goaded. One that has left
 * the battlefield or isn't a creature is not goaded.
 *
 * <p>A {@link StepKind#GOAD} step: it records the creatures it goaded and their controllers ("for each creature goaded
 * this way"). A creature the same player already goaded for as long is neither goaded again nor recorded: goading it
 * again has no effect (CR 701.15d).
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

    /**
     * "Goad all creatures [matching {@code filter}]" until your next turn: every player's creatures that match as it
     * resolves ("you don't control", "attacking", "suspected", "with the chosen name").
     */
    public static GoadTargetCreatureUntilNextTurnEffect eachCreature(PermanentPredicate filter) {
        return of(Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), Objects.requireNonNull(filter, "filter")))),
                Duration.UNTIL_YOUR_NEXT_TURN);
    }

    /**
     * "Goad each creature [player] controls" until your next turn: target player ({@code Subjects.targetPlayer()},
     * then the effect's harmful player target) or the event's player ({@code Subjects.triggeringPlayer()}, not
     * targeted).
     */
    public static GoadTargetCreatureUntilNextTurnEffect creaturesControlledBy(PlayerSubject player) {
        return of(Subjects.controlledBy(Objects.requireNonNull(player, "player"), new PermanentIsCreaturePredicate()),
                Duration.UNTIL_YOUR_NEXT_TURN);
    }

    /** "Goad each creature target player controls" until your next turn; the player is the harmful target. */
    public static GoadTargetCreatureUntilNextTurnEffect creaturesTargetPlayerControls() {
        return creaturesControlledBy(Subjects.targetPlayer());
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
        if (goaded == null) {
            return TargetSpec.harmful(TargetPredicates.creature(), targetRestriction);
        }
        return goaded.declaresPlayerTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
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
