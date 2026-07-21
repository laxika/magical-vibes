package com.github.laxika.magicalvibes.model.amount;

/**
 * Mana value of the targeted spell still on the stack at resolution
 * ({@code card.getManaValue() + stackEntry.getXValue()}), or 0 if that spell has left the stack.
 * Used by "deals damage to target spell's controller equal to that spell's mana value" (Refuse).
 */
public record TargetSpellManaValue() implements DynamicAmount {
}
