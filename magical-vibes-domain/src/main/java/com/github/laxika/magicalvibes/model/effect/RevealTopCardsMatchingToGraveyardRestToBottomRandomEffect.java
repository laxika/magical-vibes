package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top {@code count} cards of the controller's library, puts matching cards into their
 * graveyard, and puts the rest on the bottom of that library in a random order.
 */
public record RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect(
        int count, CardPredicate matcher) implements CardEffect {
}
