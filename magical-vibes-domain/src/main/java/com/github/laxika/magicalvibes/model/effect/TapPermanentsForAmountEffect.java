package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Taps as many matching untapped permanents as possible, up to the evaluated amount. If more
 * matching permanents are available than the amount, the affected player chooses exactly that many.
 *
 * @param amount the maximum number of permanents to tap
 * @param filter the permanents eligible to be tapped
 */
public record TapPermanentsForAmountEffect(DynamicAmount amount, PermanentPredicate filter)
        implements CardEffect {
}
