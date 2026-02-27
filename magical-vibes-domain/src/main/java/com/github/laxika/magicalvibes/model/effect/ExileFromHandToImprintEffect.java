package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a card matching the given predicate from the controller's hand and imprints it
 * on the source permanent. The description is used in the player prompt.
 * <p>
 * Examples:
 * - Prototype Portal: filter = CardTypePredicate(ARTIFACT), description = "an artifact card"
 * - Semblance Anvil: filter = CardNotPredicate(CardTypePredicate(LAND)), description = "a nonland card"
 */
public record ExileFromHandToImprintEffect(CardPredicate filter, String description) implements CardEffect {
}
