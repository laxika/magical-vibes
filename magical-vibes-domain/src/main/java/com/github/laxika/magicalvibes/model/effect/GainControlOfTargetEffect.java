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
 */
public record GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype,
                                        boolean tapWhenControlLost)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect(ControlDuration duration) {
        this(duration, null, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype) {
        this(duration, grantedSubtype, false);
    }

    public GainControlOfTargetEffect(ControlDuration duration, boolean tapWhenControlLost) {
        this(duration, null, tapWhenControlLost);
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
