package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC replacement effect for a player aura: if a card would be put into the enchanted player's
 * graveyard from anywhere, instead that card is revealed and put on the bottom of that player's
 * library (Wheel of Sun and Moon). Applied in {@code GraveyardService.addCardToGraveyard} by
 * scanning for a permanent carrying this effect attached to the graveyard's owner.
 */
public record RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect() implements CardEffect {
}
