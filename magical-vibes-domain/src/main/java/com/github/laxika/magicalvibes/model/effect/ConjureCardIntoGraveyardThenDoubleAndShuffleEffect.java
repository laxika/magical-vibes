package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** Conjures a card into the controller's graveyard, perpetually doubles both cards' P/T, then shuffles them. */
public record ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(
        Supplier<? extends Card> cardFactory,
        UUID dyingCardId
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(Supplier<? extends Card> cardFactory) {
        this(cardFactory, null);
    }

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffect {
        Objects.requireNonNull(cardFactory, "Conjured card factory cannot be null");
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(cardFactory, dyingCardId);
    }
}
