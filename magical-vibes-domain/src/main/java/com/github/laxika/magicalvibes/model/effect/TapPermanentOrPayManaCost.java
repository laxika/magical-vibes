package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: tap one untapped permanent matching {@code filter} or pay
 * {@code manaCost}. Exactly one option is paid. The selected permanent is carried by
 * {@code PlayCardRequest.additionalCostSacrificePermanentIds}.
 */
public record TapPermanentOrPayManaCost(
        String manaCost, PermanentPredicate filter, String description) implements CostEffect {

    private static final DynamicAmount ONE = new Fixed(1);

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public DynamicAmount tappedPermanentCount() {
        return ONE;
    }
}
