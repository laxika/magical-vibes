package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.function.Supplier;

/** Creates a fresh, non-token card in the resolving player's graveyard. */
public record ConjureCardInGraveyardEffect(Supplier<? extends Card> cardFactory) implements CardEffect {

    public ConjureCardInGraveyardEffect {
        Objects.requireNonNull(cardFactory, "Conjured card factory cannot be null");
    }
}
