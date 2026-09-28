package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Allows creature cards in the controller's hand to be cast as copies of the named card. */
public record CastCreatureCardsAsNamedCardEffect(Card card) implements CardEffect {
}
