package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Increases the persistent intensity of cards owned by the controller with the given subtype. */
public record IntensifyCardsOfSubtypeEffect(CardSubtype subtype, int amount) implements CardEffect {

    public IntensifyCardsOfSubtypeEffect(CardSubtype subtype) {
        this(subtype, 1);
    }

    public IntensifyCardsOfSubtypeEffect {
        if (subtype == null) {
            throw new IllegalArgumentException("subtype cannot be null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
}
