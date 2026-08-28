package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Offers a controller a choice between sacrificing a matching permanent and discarding a card.
 * The chosen action draws cards and gives the source permanent a temporary power/toughness boost.
 */
public record SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect(
        PermanentPredicate sacrificeFilter,
        int drawCount,
        int power,
        int toughness,
        String sacrificeDescription
) implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawCount);
    }
}
