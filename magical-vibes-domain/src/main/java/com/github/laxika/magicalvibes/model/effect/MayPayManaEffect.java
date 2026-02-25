package com.github.laxika.magicalvibes.model.effect;

/**
 * Like {@link MayEffect}, but the player must pay a mana cost to get the effect.
 * Used for "you may pay {X}. If you do, [effect]" patterns (e.g. Spellbomb cycle).
 */
public record MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt) implements CardEffect {
}
