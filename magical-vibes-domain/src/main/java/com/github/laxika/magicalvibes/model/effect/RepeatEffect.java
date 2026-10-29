package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Performs the wrapped effect the evaluated number of times as separate resolutions of that effect, so each
 * repetition is its own event ("incubate 3, X times": each incubate action creates its own token and is
 * replaced on its own).
 *
 * @param times  how many times to perform the effect
 * @param effect the effect performed each time
 */
public record RepeatEffect(DynamicAmount times, CardEffect effect) implements CardEffect {
}
