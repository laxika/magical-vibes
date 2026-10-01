package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of cards currently exiled and tracked with the source permanent or planar card. */
public record CardsExiledWithSource(CardPredicate filter) implements DynamicAmount {

    public CardsExiledWithSource() {
        this(null);
    }
}
