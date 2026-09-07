package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the dying source card, then the top card of its controller's library, and lets that
 * player choose one of those cards to play until the end of their next turn.
 */
public record ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect() implements CardEffect {
}
