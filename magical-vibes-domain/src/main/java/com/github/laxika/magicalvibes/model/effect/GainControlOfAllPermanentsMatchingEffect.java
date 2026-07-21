package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Gain permanent control of every permanent matching {@code predicate} across all battlefields
 * (Karrthus, Tyrant of Jund's "gain control of all Dragons").
 *
 * <p>Non-targeted mass control gain. At resolution the controller gains control of each matching
 * permanent they do not already control, via a per-permanent {@link GainControlOfTargetEffect}
 * floating effect on the standard layer-2 control machinery — mirroring
 * {@link GainControlOfAllLandsTargetPlayerControlsEffect}, but selected by predicate rather than
 * by a target player.
 *
 * @param predicate narrows which permanents (any controller) are seized
 */
public record GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate) implements CardEffect {
}
