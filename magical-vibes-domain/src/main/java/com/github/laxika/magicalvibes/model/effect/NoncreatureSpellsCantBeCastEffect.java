package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (global, symmetric): no player can cast noncreature spells whose mana value is
 * {@code minManaValue} or greater. When {@code restrictXSpells} is true, no player can cast a
 * noncreature spell with {@code {X}} in its mana cost either (regardless of its mana value).
 *
 * <p>Gaddock Teeg uses {@code (4, true)}. Applies to every player, including the source's controller.
 */
public record NoncreatureSpellsCantBeCastEffect(int minManaValue, boolean restrictXSpells) implements CardEffect {
}
