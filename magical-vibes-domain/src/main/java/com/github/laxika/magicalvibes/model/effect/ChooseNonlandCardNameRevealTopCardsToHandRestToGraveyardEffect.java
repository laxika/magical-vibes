package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a nonland card name. Reveal the top N cards of your library and put all of them with
 * that name into your hand. Put the rest into your graveyard."
 *
 * @param count number of cards revealed from the top of the controller's library
 */
public record ChooseNonlandCardNameRevealTopCardsToHandRestToGraveyardEffect(int count)
        implements CardEffect {
}
