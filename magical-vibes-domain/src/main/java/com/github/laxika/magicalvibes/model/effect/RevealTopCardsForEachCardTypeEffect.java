package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top {@code count} cards of the controller's library, then offers at most one card
 * for each card type represented among the revealed cards. Unchosen cards are put on the bottom of
 * the library in a random order.
 *
 * @param count number of cards to reveal
 */
public record RevealTopCardsForEachCardTypeEffect(int count) implements CardEffect {
}
