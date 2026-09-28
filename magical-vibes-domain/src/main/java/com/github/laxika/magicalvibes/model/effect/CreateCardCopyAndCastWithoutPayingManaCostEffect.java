package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.function.Supplier;

/** Creates one fixed card copy and immediately casts it without paying its mana cost. */
public record CreateCardCopyAndCastWithoutPayingManaCostEffect(Supplier<? extends Card> cardFactory)
        implements CardEffect {

    public CreateCardCopyAndCastWithoutPayingManaCostEffect {
        Objects.requireNonNull(cardFactory, "Card factory cannot be null");
    }
}
