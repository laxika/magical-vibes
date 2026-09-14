package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Gains control of target permanent for the given {@link ControlDuration}.
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
 * @param linkStolenPermanentToSource when true, the stolen permanent's id is recorded on the source
 *                           permanent ({@code Permanent.chosenPermanentId}) so a later trigger on
 *                           the source can still refer to "that creature" after the control effect
 *                           is gone (Merieke Ri Berit's "destroy that creature" on leave/untap).
 *                           Only meaningful for the {@code WHILE_SOURCE_*} durations.
 * @param targetPredicate optional additional restriction on the target permanent
 * @param maintainTargetPredicate when true, the target predicate is also maintained for the
 *                           duration of the control effect (e.g. Old Man of the Sea's power check)
 */
public record GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype,
                                        boolean tapWhenControlLost, boolean linkStolenPermanentToSource,
                                        PermanentPredicate targetPredicate, boolean opponentChoosesTarget,
                                        boolean maintainTargetPredicate)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect(ControlDuration duration) {
        this(duration, null, false, false, null, false, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype) {
        this(duration, grantedSubtype, false, false, null, false, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, boolean tapWhenControlLost) {
        this(duration, null, tapWhenControlLost, false, null, false, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype,
                                     boolean tapWhenControlLost, boolean linkStolenPermanentToSource) {
        this(duration, grantedSubtype, tapWhenControlLost, linkStolenPermanentToSource, null, false, false);
    }

    /** Control for the given duration, recording the stolen permanent on the source (Merieke Ri Berit). */
    public static GainControlOfTargetEffect linkingToSource(ControlDuration duration) {
        return new GainControlOfTargetEffect(duration, null, false, true, null, false, false);
    }

    /** Control for the given duration, with an additional target restriction. */
    public static GainControlOfTargetEffect withTargetPredicate(ControlDuration duration,
                                                                PermanentPredicate targetPredicate) {
        return new GainControlOfTargetEffect(duration, null, false, false, targetPredicate, false, false);
    }

    /** Control while the source remains tapped and the target continues to match the predicate. */
    public static GainControlOfTargetEffect whileSourceRemainsTappedAndTargetMatches(
            PermanentPredicate targetPredicate) {
        return new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS_TAPPED,
                null, false, false, targetPredicate, false, true);
    }

    /** Control of a creature chosen by an opponent who was chosen by the spell's controller. */
    public static GainControlOfTargetEffect opponentChosenTarget(ControlDuration duration) {
        return new GainControlOfTargetEffect(duration, null, false, false, null, true, false);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }
}
