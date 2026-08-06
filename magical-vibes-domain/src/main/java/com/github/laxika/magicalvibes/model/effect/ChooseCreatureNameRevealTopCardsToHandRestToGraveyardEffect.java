package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature card name. Reveal the top N cards of your library and put all of them with
 * that name into your hand. Put the rest into your graveyard."
 *
 * <p>Used by Wood Sage ({@code N = 4}). The name is chosen on resolution, before the cards are
 * revealed; only creature card names are offered.
 *
 * @param count number of cards revealed from the top of the controller's library
 */
public record ChooseCreatureNameRevealTopCardsToHandRestToGraveyardEffect(int count) implements CardEffect {
}
