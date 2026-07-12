package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires the player to discard a card matching a predicate.
 *
 * @param predicate        optional predicate cards must match (null = any card)
 * @param label            human-readable label for the card quality (e.g. "land", "historic"), used in UI messages
 * @param manaValueEqualsX when true, the discarded card's mana value must equal the ability's chosen X
 *                         (e.g. Knollspine Invocation "Discard a card with mana value X")
 */
public record DiscardCardTypeCost(CardPredicate predicate, String label, boolean manaValueEqualsX) implements CostEffect {

    public DiscardCardTypeCost(CardPredicate predicate, String label) {
        this(predicate, label, false);
    }
}
