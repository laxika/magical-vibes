package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Simultaneously flips the tap state of every matching permanent on every battlefield. The
 * matching permanents are snapshotted before either half is applied, so untapping tapped matches
 * cannot make them eligible for the tapping half.
 *
 * @param filter selects which permanents flip
 */
public record SimultaneouslyFlipAllPermanentsTapStatesEffect(PermanentPredicate filter)
        implements CardEffect {
}
