package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Records a perpetual power/toughness boost for matching cards owned by the effect controller. */
public record PerpetuallyBoostOwnedCardsEffect(CardPredicate filter, int powerBoost, int toughnessBoost)
        implements CardEffect {
}
