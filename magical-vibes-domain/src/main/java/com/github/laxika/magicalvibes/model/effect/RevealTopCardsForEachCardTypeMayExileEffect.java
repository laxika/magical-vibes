package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reveals the top {@code count} cards of the controller's library and offers at most one card of
 * each card type to exile. The other revealed cards are put into the controller's graveyard. If at
 * least four cards were exiled, one exiled spell may be cast without paying its mana cost; the
 * remaining cards exiled by this effect are then put into their owners' hands.
 *
 * @param count number of cards to reveal
 */
public record RevealTopCardsForEachCardTypeMayExileEffect(DynamicAmount count) implements CardEffect {
}
