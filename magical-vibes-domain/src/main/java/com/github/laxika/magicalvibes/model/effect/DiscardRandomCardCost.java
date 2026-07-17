package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that discards a card chosen at random from the controller's hand as part of an
 * activated ability's cost (e.g. Coral Helm). Unlike {@link DiscardCardTypeCost} there is no
 * player choice — a random card is removed on activation. The hand must contain at least one card
 * to pay the cost. Fires the discarded card's discard triggers.
 */
public record DiscardRandomCardCost() implements CostEffect {
}
