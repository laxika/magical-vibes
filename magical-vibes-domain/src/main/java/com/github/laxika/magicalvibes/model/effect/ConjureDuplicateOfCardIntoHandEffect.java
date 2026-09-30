package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;

/** Conjures a duplicate of a captured card into the effect controller's hand. */
public record ConjureDuplicateOfCardIntoHandEffect(Card card) implements CardEffect {

    public ConjureDuplicateOfCardIntoHandEffect {
        Objects.requireNonNull(card, "card");
    }
}
