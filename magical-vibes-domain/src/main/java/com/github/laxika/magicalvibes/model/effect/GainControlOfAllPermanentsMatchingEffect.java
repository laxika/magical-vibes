package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Gain control of every permanent matching {@code predicate} across all battlefields (Karrthus,
 * Tyrant of Jund's "gain control of all Dragons"; Tibalt, the Fiend-Blooded's "gain control of all
 * creatures until end of turn").
 *
 * <p>Non-targeted mass control gain. At resolution the controller gains control of each matching
 * permanent they do not already control, via a per-permanent {@link GainControlOfTargetEffect}
 * floating effect on the standard layer-2 control machinery — mirroring
 * {@link GainControlOfAllLandsTargetPlayerControlsEffect}, but selected by predicate rather than
 * by a target player.
 *
 * @param predicate narrows which permanents (any controller) are seized
 * @param duration  how long the control gain lasts
 */
public record GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate, ControlDuration duration)
        implements CardEffect {

    public GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate) {
        this(predicate, ControlDuration.PERMANENT);
    }
}
