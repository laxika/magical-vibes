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
 * <p>A {@link com.github.laxika.magicalvibes.model.StepKind#GAIN_CONTROL} step: it records the permanents whose
 * controller it changed ("draw a card for each one they gained control of this way", "when you do").
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
 * @param gainer             who gains control: {@code null} for the effect's controller ("gain control of"), or the
 *                           player it names ("target player gains control of up to three target creatures until end
 *                           of turn", "target opponent gains control of all other permanents you control", "target
 *                           opponent chosen at random gains control of this enchantment" with
 *                           {@code targetPlayerChosenAtRandom()}); another
 *                           player gains control only permanently or until end of turn, with no rider
 */
public record GainControlOfTargetEffect(Duration duration, CardSubtype grantedSubtype,
                                        boolean tapWhenControlLost,
                                        PermanentPredicate targetPredicate, boolean opponentChoosesTarget,
                                        boolean maintainTargetPredicate, PermanentSubject subject,
                                        PlayerSubject gainer)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect {
        Objects.requireNonNull(subject, "subject");
        if (!(subject instanceof PermanentSubject.Target) && subject.declaresTarget()) {
            throw new IllegalArgumentException("Control is gained of the target or of no target: " + subject);
        }
        if (gainer != null && !(gainer instanceof PlayerSubject.You)
                && (!(Duration.PERMANENT.equals(duration) || Duration.UNTIL_END_OF_TURN.equals(duration))
                        || grantedSubtype != null || tapWhenControlLost || opponentChoosesTarget)) {
            throw new IllegalArgumentException("Another player gains control permanently or until end of turn: "
                    + gainer + " " + duration);
        }
    }

    public GainControlOfTargetEffect(Duration duration, CardSubtype grantedSubtype,
                                     boolean tapWhenControlLost, PermanentPredicate targetPredicate,
                                     boolean opponentChoosesTarget, boolean maintainTargetPredicate,
                                     PermanentSubject subject) {
        this(duration, grantedSubtype, tapWhenControlLost, targetPredicate, opponentChoosesTarget,
                maintainTargetPredicate, subject, null);
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

    /** The same control change, to {@code player} instead of the effect's controller. */
    public GainControlOfTargetEffect gainedBy(PlayerSubject player) {
        return new GainControlOfTargetEffect(duration, grantedSubtype, tapWhenControlLost, targetPredicate,
                opponentChoosesTarget, maintainTargetPredicate, subject, Objects.requireNonNull(player, "player"));
    }

    /** Who gains control. */
    public PlayerSubject gainingPlayer() {
        return gainer == null ? Subjects.you() : gainer;
    }

    /** The duration of the steal; {@code null} when another player than the controller gains control (no steal). */
    @Override
    public Duration controlDuration() {
        return gainingPlayer() instanceof PlayerSubject.You ? duration : null;
    }

    /** The permanent target, else the player target the gainer declares ("target opponent gains control of …"). */
    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
        }
        if (gainer == null || !gainer.declaresTarget()) {
            return TargetSpec.NONE;
        }
        TargetSpec spec = TargetSpec.benign(TargetPredicates.player());
        return gainer.targetChosenAtRandom() ? spec.atRandom() : spec;
    }
}
