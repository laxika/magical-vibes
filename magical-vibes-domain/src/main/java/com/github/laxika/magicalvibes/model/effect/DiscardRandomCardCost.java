package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that discards a card chosen at random from the controller's hand as part of a spell
 * or activated ability's cost (e.g. Sonic Burst or Coral Helm). Unlike
 * {@link DiscardCardTypeCost} there is no player choice — a random card is removed when the cost
 * is paid. The hand must contain at least one card to pay the cost. Fires the discarded card's
 * discard triggers.
 * Cost effect that discards a card chosen at random from the controller's hand as part of an
 * activated ability's or spell's cost (e.g. Coral Helm and Acceptable Losses). Unlike
 * {@link DiscardCardTypeCost} there is no
 * player choice — a random card is removed on activation. The hand must contain at least one card
 * to pay the cost. Fires the discarded card's discard triggers.
 * When used as a spell's additional cost, it is paid during casting.
 */
public record DiscardRandomCardCost() implements CostEffect {
}
