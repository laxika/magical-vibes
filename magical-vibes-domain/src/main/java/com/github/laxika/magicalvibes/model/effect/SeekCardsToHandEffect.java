package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly seeks up to the evaluated number of matching cards from the controller's library to hand. */
public record SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter, ManaValueBound manaValueBound)
        implements CardEffect {

    public SeekCardsToHandEffect(DynamicAmount amount, CardPredicate filter) {
        this(amount, filter, null);
    }

    public SeekCardsToHandEffect(CardPredicate filter) {
        this(new EventValue(), filter, null);
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue
                || manaValueBound != null && manaValueBound.amount() instanceof EventValue;
    }
}
