package com.github.laxika.magicalvibes.model.amount;

/**
 * Mana value of the targeted spell, or the spell that caused a spell-cast trigger, still on the stack at resolution
 * ({@code card.getManaValue() + stackEntry.getXValue()}), or 0 if that spell has left the stack.
 * Used by effects such as Refuse and Tecutlan, the Searing Rift.
 */
public record TargetSpellManaValue() implements DynamicAmount {
}
