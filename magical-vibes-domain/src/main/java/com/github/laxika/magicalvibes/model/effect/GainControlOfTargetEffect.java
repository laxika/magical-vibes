package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

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
 */
public record GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype,
                                        boolean tapWhenControlLost, boolean linkStolenPermanentToSource)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect(ControlDuration duration) {
        this(duration, null, false, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype) {
        this(duration, grantedSubtype, false, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, boolean tapWhenControlLost) {
        this(duration, null, tapWhenControlLost, false);
    }

    /** Control for the given duration, recording the stolen permanent on the source (Merieke Ri Berit). */
    public static GainControlOfTargetEffect linkingToSource(ControlDuration duration) {
        return new GainControlOfTargetEffect(duration, null, false, true);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
