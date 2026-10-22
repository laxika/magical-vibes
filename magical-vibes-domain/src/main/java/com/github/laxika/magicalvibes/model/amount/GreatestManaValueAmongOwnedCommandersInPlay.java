package com.github.laxika.magicalvibes.model.amount;

/**
 * The greatest mana value of a commander its controller owns that is on the battlefield or in the
 * command zone. Commanders owned in hand, library, graveyard or exile are not counted.
 */
public record GreatestManaValueAmongOwnedCommandersInPlay() implements DynamicAmount {
}
