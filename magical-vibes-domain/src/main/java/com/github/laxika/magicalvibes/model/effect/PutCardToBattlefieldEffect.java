package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller put a card from their hand onto the battlefield.
 * <p>
 * The {@code predicate} filters which cards in hand are valid choices, and
 * the {@code label} is used in the prompt shown to the player (e.g. "creature", "land",
 * "historic permanent").
 * <p>
 * Typically wrapped in a {@link MayEffect} for "you may put" wording.
 *
 * @param predicate filter for eligible cards in hand (e.g. {@code CardTypePredicate(CREATURE)},
 *                  {@code CardAllOfPredicate(CardIsHistoricPredicate, CardIsPermanentPredicate)})
 * @param label     human-readable description of the card type for prompts (e.g. "creature", "historic permanent")
 */
public record PutCardToBattlefieldEffect(CardPredicate predicate, String label) implements CardEffect {
}
