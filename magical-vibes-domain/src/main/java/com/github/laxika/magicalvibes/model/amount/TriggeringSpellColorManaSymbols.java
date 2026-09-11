package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * The number of mana symbols containing the given color in the spell that caused the current
 * triggered ability.
 */
public record TriggeringSpellColorManaSymbols(ManaColor color) implements DynamicAmount {
}
