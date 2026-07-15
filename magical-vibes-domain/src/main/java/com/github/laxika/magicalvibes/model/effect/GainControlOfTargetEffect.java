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
 * @param duration       how long control is retained
 * @param grantedSubtype if non-null, this subtype is permanently added to the stolen
 *                       permanent (e.g. Captivating Vampire's "It becomes a Vampire in
 *                       addition to its other types"). Only applied for {@code PERMANENT}
 *                       duration, the only duration any card grants a subtype with.
 */
public record GainControlOfTargetEffect(ControlDuration duration, CardSubtype grantedSubtype)
        implements ControlStealingEffect {

    public GainControlOfTargetEffect(ControlDuration duration) {
        this(duration, null);
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
