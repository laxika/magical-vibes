package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps as many matching permanents as possible, up to the evaluated amount. If more matching
 * permanents are available than the amount, the affected player chooses exactly that many.
 *
 * @param amount the maximum number of permanents to untap
 * @param filter the permanents eligible to be untapped
 */
public record UntapPermanentsForAmountEffect(DynamicAmount amount, PermanentPredicate filter)
        implements CardEffect {
}
