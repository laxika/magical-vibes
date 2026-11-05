package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Conjures a card into the controller's graveyard, perpetually doubles both cards' P/T, then shuffles them. The
 * other card is the one that died, the stack entry's triggering card.
 */
public record ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(Supplier<? extends Card> cardFactory)
        implements CardEffect {

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffect {
        Objects.requireNonNull(cardFactory, "Conjured card factory cannot be null");
    }
}
