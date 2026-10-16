package com.github.laxika.magicalvibes.model.effect;

/**
 * Stores the mana value of the target spell as the resolving spell's X value, so later effects in
 * the same resolution can read it after that spell has been countered and left the stack
 * (Hurl into History).
 */
public record RecordTargetSpellManaValueAsXEffect() implements CardEffect {
}
