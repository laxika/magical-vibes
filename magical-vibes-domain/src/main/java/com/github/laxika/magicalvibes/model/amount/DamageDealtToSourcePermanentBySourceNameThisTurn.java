package com.github.laxika.magicalvibes.model.amount;

/**
 * The total damage actually dealt to the source permanent this turn by other sources with the
 * given name.
 */
public record DamageDealtToSourcePermanentBySourceNameThisTurn(String sourceName) implements DynamicAmount {
}
