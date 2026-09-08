package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Draws cards only when the permanent sacrificed as a spell's additional cost matches a filter.
 * The sacrificed card is read from the stack entry's last-known snapshot.
 */
public record DrawCardIfSacrificedCardMatchesEffect(CardPredicate filter, DynamicAmount amount)
        implements CardDrawingEffect {

    public DrawCardIfSacrificedCardMatchesEffect(CardPredicate filter) {
        this(filter, new Fixed(1));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
