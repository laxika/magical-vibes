package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Simultaneously flips the tap state of matching permanents the targeted player controls: each
 * tapped match untaps and each untapped match taps, based on a pre-resolution snapshot so the
 * halves do not cascade into each other (Sands of Time). The acting player is the stack entry's
 * {@code targetId} ({@code EACH_UPKEEP_TRIGGERED} sets the active player). Non-targeting beyond that.
 *
 * @param filter selects which controlled permanents flip (artifact / creature / land for Sands of Time)
 */
public record SimultaneouslyFlipControlledPermanentsTapStatesEffect(PermanentPredicate filter)
        implements CardEffect {
}
