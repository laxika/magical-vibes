package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may shuffle up to one card matching {@code filter} from your graveyard into your library."
 *
 * <p>Resolution-time optional choice: prompts the controller to pick a single card from their own
 * graveyard (matching {@code filter}, or any card when {@code filter} is {@code null}) to shuffle
 * into their library, or to decline. Non-targeted at cast time, so it can be paired with an effect
 * that targets a spell on the stack (e.g. {@link CounterSpellEffect}). Used by Put Away.
 */
public record ShuffleCardFromControllerGraveyardIntoLibraryEffect(CardPredicate filter) implements CardEffect {
}
