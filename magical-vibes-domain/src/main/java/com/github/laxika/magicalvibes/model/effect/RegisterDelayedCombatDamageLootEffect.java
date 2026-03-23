package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "Whenever one or more creatures you control deal combat damage to a player this turn,
 * draw a card, then discard a card."
 *
 * <p>The delayed trigger fires once per combat damage step (not per creature) when at least
 * one creature the controller controls deals combat damage to any player.
 *
 * @param drawAmount    number of cards to draw when the trigger fires
 * @param discardAmount number of cards to discard after drawing
 */
public record RegisterDelayedCombatDamageLootEffect(int drawAmount, int discardAmount) implements CardEffect {
}
