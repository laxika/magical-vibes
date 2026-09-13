package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants devour to the creature spell that caused this trigger while it is still on the stack.
 * The numeric grant is carried onto the entering permanent and handled by the existing devour
 * as-enters choice flow.
 */
public record GrantDevourToCastSpellEffect(int multiplier) implements CardEffect {
}
