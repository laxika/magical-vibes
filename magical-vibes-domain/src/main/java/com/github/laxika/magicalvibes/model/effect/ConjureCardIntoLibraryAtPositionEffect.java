package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.function.Supplier;

/** Conjures a fresh card at a fixed zero-based position from the top of a library. */
public record ConjureCardIntoLibraryAtPositionEffect(
        Supplier<? extends Card> cardFactory,
        int position) implements CardEffect {

    public ConjureCardIntoLibraryAtPositionEffect {
        Objects.requireNonNull(cardFactory, "Conjured card factory cannot be null");
        if (position < 0) {
            throw new IllegalArgumentException("Library position cannot be negative");
        }
    }
}
