package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of lands on the battlefield (any player's) whose name matches the name of the
 * card imprinted on the source permanent. Evaluates to 0 when the source has no imprinted card.
 * Models Strata Scythe's "+1/+1 for each land on the battlefield with the same name as the
 * exiled card".
 */
public record LandsMatchingImprintedName() implements DynamicAmount {
}
