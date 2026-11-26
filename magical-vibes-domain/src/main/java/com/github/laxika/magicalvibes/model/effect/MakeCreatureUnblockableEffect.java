package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "[Permanent] can't be blocked [this turn]": each permanent {@code subject} names can't be blocked for
 * {@code duration} (CR 509.1b). The effect is a rule about that permanent (CR 611.2a, 613.11), not an ability it gains,
 * so a later "loses all abilities" doesn't end it.
 *
 * <p>The subject is the effect's target ({@link #MakeCreatureUnblockableEffect()}, a creature, optionally narrowed by
 * {@code filter}; {@link #targetPermanent}, any permanent) or one permanent it doesn't target: {@link #self()} ("this
 * creature"), {@code of(Subjects.attached())} ("equipped creature"), {@code of(Subjects.triggering())} ("it", "that
 * creature"). A set ("creatures you control can't be blocked this turn") is refused: such an effect changes a rule
 * and also covers creatures that arrive later (CR 611.2c), which is {@link MakeAllCreaturesUnblockableEffect}.
 *
 * <p>{@link #byCreaturesYourOpponentsControl()}: "can't be blocked by creatures your opponents control" instead.
 *
 * @param subject           the permanents made unblockable; a target subject declares the benign target
 * @param duration          how long ({@link Duration#UNTIL_END_OF_TURN} for "this turn")
 * @param filter            the target's restriction, or {@code null}; only for a target
 * @param targetsPermanent  the target may be any permanent, not only a creature; only for a target
 * @param opponentsCreaturesOnly only creatures the ability controller's opponents control can't block it
 */
public record MakeCreatureUnblockableEffect(PermanentSubject subject, Duration duration, PermanentPredicate filter,
                                            boolean targetsPermanent, boolean opponentsCreaturesOnly)
        implements CardEffect {

    public MakeCreatureUnblockableEffect {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(duration, "duration");
        if (subject.isSet() || subject.declaresPlayerTarget()) {
            throw new IllegalArgumentException("A set made unblockable is a rule that isn't locked (CR 611.2c): "
                    + "use MakeAllCreaturesUnblockableEffect, not " + subject);
        }
        if (!subject.declaresTarget() && (filter != null || targetsPermanent)) {
            throw new IllegalArgumentException("Only a target subject takes a target restriction: " + subject);
        }
    }

    /** "Target creature can't be blocked this turn." */
    public MakeCreatureUnblockableEffect() {
        this((PermanentPredicate) null);
    }

    /** "Target creature [matching {@code filter}] can't be blocked this turn." */
    public MakeCreatureUnblockableEffect(PermanentPredicate filter) {
        this(Subjects.target(), Duration.UNTIL_END_OF_TURN, filter, false, false);
    }

    /** "This creature can't be blocked this turn." */
    public static MakeCreatureUnblockableEffect self() {
        return self(Duration.UNTIL_END_OF_TURN);
    }

    /** "This creature can't be blocked [this combat]." */
    public static MakeCreatureUnblockableEffect self(Duration duration) {
        return of(Subjects.source(), duration);
    }

    /** "[The permanent {@code subject} names] can't be blocked this turn." */
    public static MakeCreatureUnblockableEffect of(PermanentSubject subject) {
        return of(subject, Duration.UNTIL_END_OF_TURN);
    }

    /** "[Until your next turn,] [the permanent {@code subject} names] can't be blocked." */
    public static MakeCreatureUnblockableEffect of(PermanentSubject subject, Duration duration) {
        return new MakeCreatureUnblockableEffect(subject, duration, null, false, false);
    }

    /** "Target [Ninja or Turtle] can't be blocked this turn": a target that may be a noncreature permanent. */
    public static MakeCreatureUnblockableEffect targetPermanent(PermanentPredicate filter) {
        return new MakeCreatureUnblockableEffect(Subjects.target(), Duration.UNTIL_END_OF_TURN, filter, true, false);
    }

    /** "... can't be blocked by creatures your opponents control" instead of by any creature. */
    public MakeCreatureUnblockableEffect byCreaturesYourOpponentsControl() {
        return new MakeCreatureUnblockableEffect(subject, duration, filter, targetsPermanent, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(targetsPermanent ? TargetPredicates.permanent() : TargetPredicates.creature(),
                    filter);
        }
        return subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Attached
                || subject instanceof PermanentSubject.Triggering
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }

    /** An Equipment's or Aura's ability captures its host as it is activated, before a sacrifice cost severs it. */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
