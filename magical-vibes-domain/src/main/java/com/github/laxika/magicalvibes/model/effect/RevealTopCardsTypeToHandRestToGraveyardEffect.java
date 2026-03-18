package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Reveal the top N cards of your library. Put all cards matching the specified types
 * into your hand and the rest into your graveyard.
 * Used by Mulch (lands), and reusable for similar "reveal and sort by type" effects.
 *
 * @param count     number of cards to reveal
 * @param cardTypes card types that go to hand (e.g. {@code Set.of(CardType.LAND)})
 */
public record RevealTopCardsTypeToHandRestToGraveyardEffect(
        int count,
        Set<CardType> cardTypes
) implements CardEffect {
}
