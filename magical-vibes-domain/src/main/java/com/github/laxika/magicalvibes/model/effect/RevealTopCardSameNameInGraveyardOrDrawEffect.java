package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If its name is present in that player's
 * graveyard, it is put into the graveyard; otherwise the controller draws a card.
 */
public record RevealTopCardSameNameInGraveyardOrDrawEffect() implements CardEffect {
}
