package com.github.laxika.magicalvibes.model.amount;

/**
 * The largest hand size among the opponents of the controller of the source object.
 * Evaluates to zero when the controller is unknown or there are no opponents.
 */
public record GreatestOpponentHandSize() implements DynamicAmount {
}
