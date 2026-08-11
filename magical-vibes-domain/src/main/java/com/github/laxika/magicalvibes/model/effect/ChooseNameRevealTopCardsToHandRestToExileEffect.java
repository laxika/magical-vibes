package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a card name other than a basic land card name. Reveal the top N cards of your library
 * and put all of them with that name into your hand. Exile the rest."
 *
 * <p>The name is chosen on resolution before the cards are revealed. Only non-basic-land card
 * names are offered.
 *
 * @param count number of cards revealed from the top of the controller's library
 */
public record ChooseNameRevealTopCardsToHandRestToExileEffect(int count) implements CardEffect {
}
