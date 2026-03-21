package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires the player to discard a card matching a predicate.
 *
 * @param predicate optional predicate cards must match (null = any card)
 * @param label     human-readable label for the card quality (e.g. "land", "historic"), used in UI messages
 */
public record DiscardCardTypeCost(CardPredicate predicate, String label) implements CostEffect {
}
