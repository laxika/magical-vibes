package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CastingOption;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Conjures a fresh card at a random position among the specified top cards of a library. */
public record ConjureCardIntoTopCardsOfLibraryEffect(
        Supplier<? extends Card> cardFactory,
        int count,
        List<CastingOption> addedCastingOptions) implements CardEffect {

    public ConjureCardIntoTopCardsOfLibraryEffect {
        Objects.requireNonNull(cardFactory, "Conjured card factory cannot be null");
        if (count < 1) {
            throw new IllegalArgumentException("Library top count must be positive");
        }
        addedCastingOptions = addedCastingOptions == null
                ? List.of()
                : List.copyOf(addedCastingOptions);
    }

    public ConjureCardIntoTopCardsOfLibraryEffect(Supplier<? extends Card> cardFactory, int count) {
        this(cardFactory, count, List.of());
    }
}
