package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a creature card is revealed.
 * That card is put onto the battlefield, and all other revealed cards are shuffled into the
 * controller's library.
 */
public record RevealUntilCreatureToBattlefieldRestToLibraryEffect() implements CardEffect {
}
