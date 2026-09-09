package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot, turn-scoped replacement of the controller's next draw with putting a card
 * they own from outside the game into their hand.
 */
public record RegisterNextDrawFromOutsideGameReplacementEffect() implements CardEffect {
}
