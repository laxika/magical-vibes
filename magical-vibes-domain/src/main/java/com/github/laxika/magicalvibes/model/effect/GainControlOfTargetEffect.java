package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.Objects;

/**
 * Gains control of target permanent, or of the permanents {@link #subject()} names, for the given
 * {@link Duration}.
 *
 * <p>The card's own target filter handles type restrictions (e.g. creature-only for
 * Threaten, artifact-only for Metallic Mastery, Vampire-only for Olivia Voldaren).
 * Haste/untap riders (Act of Treason pattern) are composed as separate effects on the
 * card, not baked into this effect.
 *
 * @param duration           how long control is retained
 * @param grantedSubtype     if non-null, this subtype is permanently added to the stolen
 *                           permanent (e.g. Captivating Vampire's "It becomes a Vampire in
 *                           addition to its other types"). Only applied for {@code PERMANENT}
 *                           duration, the only duration any card grants a subtype with.
 * @param tapWhenControlLost when true, the stolen permanent is tapped when the temporary control
 *                           effect expires and it reverts to its owner (Magus of the Unseen's
 *                           "When you lose control of the artifact, tap it"). Only meaningful for
 *                           {@code END_OF_TURN} duration.
 * @param targetPredicate optional additional restriction on the target permanent
 * @param maintainTargetPredicate when true, the target predicate is also maintained for the
 *                           duration of the control effect (e.g. Old Man of the Sea's power check)
 * @param subject            {@code target()}, the declared target, or permanents that are no target: {@code triggering()}
 *                           (Treasure Nabber's "gain control of that artifact"), the ones an earlier step chose
 *                           ("choose a creature at random. You gain control of that creature")
 */
public record GainControlOfTargetEffect(Duration duration, CardSubtype grantedSubtype,
                                        boolean tapWhenControlLost,
                                        PermanentPredicate targetPredicate, boolean opponentChoosesTarget,
                                        boolean maintainTargetPredicate, PermanentSubject subject)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect {
        Objects.requireNonNull(subject, "subject");
        if (!(subject instanceof PermanentSubject.Target) && subject.declaresTarget()) {
            throw new IllegalArgumentException("Control is gained of the target or of no target: " + subject);
        }
    }

    public GainControlOfTargetEffect(Duration duration, CardSubtype grantedSubtype,
                                     boolean tapWhenControlLost, PermanentPredicate targetPredicate,
                                     boolean opponentChoosesTarget, boolean maintainTargetPredicate) {
        this(duration, grantedSubtype, tapWhenControlLost, targetPredicate, opponentChoosesTarget,
                maintainTargetPredicate, Subjects.target());
    }

    /** "Gain control of that permanent" for the given duration: the trigger event's permanent, no target. */
    public static GainControlOfTargetEffect ofTriggeringPermanent(Duration duration) {
        return of(Subjects.triggering(), duration);
    }

    /** Control of the permanents {@code subject} names, none of them targeted. */
    public static GainControlOfTargetEffect of(PermanentSubject subject, Duration duration) {
        return new GainControlOfTargetEffect(duration, null, false, null, false, false, subject);
    }

    public GainControlOfTargetEffect(Duration duration) {
        this(duration, null, false, null, false, false);
    }

    public GainControlOfTargetEffect(Duration duration, CardSubtype grantedSubtype) {
        this(duration, grantedSubtype, false, null, false, false);
    }

    public GainControlOfTargetEffect(Duration duration, boolean tapWhenControlLost) {
        this(duration, null, tapWhenControlLost, null, false, false);
    }

    /** Control for the given duration, with an additional target restriction. */
    public static GainControlOfTargetEffect withTargetPredicate(Duration duration,
                                                                PermanentPredicate targetPredicate) {
        return new GainControlOfTargetEffect(duration, null, false, targetPredicate, false, false);
    }

    /** Control while the source remains tapped and the target continues to match the predicate. */
    public static GainControlOfTargetEffect whileSourceRemainsTappedAndTargetMatches(
            PermanentPredicate targetPredicate) {
        return new GainControlOfTargetEffect(Duration.WHILE_SOURCE_REMAINS_TAPPED,
                null, false, targetPredicate, false, true);
    }

    /** Control of a creature chosen by an opponent who was chosen by the spell's controller. */
    public static GainControlOfTargetEffect opponentChosenTarget(Duration duration) {
        return new GainControlOfTargetEffect(duration, null, false, null, true, false);
    }

    @Override
    public Duration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent(), targetPredicate) : TargetSpec.NONE;
    }
}
