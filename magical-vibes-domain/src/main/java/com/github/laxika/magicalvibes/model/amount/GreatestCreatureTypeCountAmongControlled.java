package com.github.laxika.magicalvibes.model.amount;

/**
 * The greatest number of creatures the controller controls that share a creature type,
 * including the source creature when it is still controlled at resolution.
 */
public record GreatestCreatureTypeCountAmongControlled() implements DynamicAmount {
}
