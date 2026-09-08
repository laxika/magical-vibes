package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top {@code count} cards of the controller's library, then offers at most one card
 * for each card type among that controller's noncreature spells cast this turn. Unchosen cards are
 * put on the bottom of the library in a random order.
 *
 * <p>The dynamic card-type list is evaluated when this effect resolves, while the spell-cast
 * condition that causes Hurkyl's ability to trigger is represented by the card's conditional
 * end-step trigger.</p>
 *
 * @param count number of cards to reveal
 */
public record RevealTopCardsForCardTypesCastThisTurnEffect(int count) implements CardEffect {
}
