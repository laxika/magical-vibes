package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells whose mana value is at most {@code maxManaValue}. The chosen value of X is
 * included, since any X in a spell's mana cost equals the announced value while it is on the
 * stack (CR 107.3a) — "counter target
 * spell with mana value 4 or less" (Thoughtbind).
 */
public record StackEntryMaxManaValuePredicate(int maxManaValue) implements StackEntryPredicate {
}
