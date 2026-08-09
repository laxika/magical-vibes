package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Taps every permanent matching {@code filter} except the source permanent and prevents those
 * permanents from untapping for as long as the source remains tapped.
 *
 * @param filter selects the other permanents affected by the effect
 */
public record TapAndLockOtherPermanentsEffect(PermanentPredicate filter) implements CardEffect {
}
