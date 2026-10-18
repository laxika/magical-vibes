package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps matching permanents up to the evaluated amount. If more matching permanents are available
 * than the amount, the affected player chooses permanents to untap: exactly that many when
 * {@code upTo} is false, or any number from zero up to that many when it is true ("untap up to X").
 *
 * @param amount the maximum number of permanents to untap
 * @param filter the permanents eligible to be untapped
 * @param upTo whether the affected player may choose fewer permanents than the amount
 */
public record UntapPermanentsForAmountEffect(DynamicAmount amount, PermanentPredicate filter, boolean upTo)
        implements CardEffect {

    public UntapPermanentsForAmountEffect(DynamicAmount amount, PermanentPredicate filter) {
        this(amount, filter, false);
    }
}
