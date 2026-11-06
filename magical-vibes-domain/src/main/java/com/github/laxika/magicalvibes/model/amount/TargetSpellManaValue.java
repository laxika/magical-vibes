package com.github.laxika.magicalvibes.model.amount;

/**
 * Mana value of the targeted spell, or the spell that caused a spell-cast trigger, on the stack at resolution
 * ({@code card.getManaValue() + stackEntry.getXValue()}). Once the triggering spell has left the stack it is that
 * spell's mana value as it was when the ability triggered (the stack entry's triggering spell, CR 608.2h); with
 * neither, the entry's event value. "That spell's mana value": Refuse, Tecutlan, the Searing Rift, Geometric Nexus,
 * Saruman, the White Hand.
 */
public record TargetSpellManaValue() implements DynamicAmount {
}
