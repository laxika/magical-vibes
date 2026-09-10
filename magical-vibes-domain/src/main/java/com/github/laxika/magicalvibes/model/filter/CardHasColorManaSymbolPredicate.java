package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Matches cards whose mana cost contains at least one mana symbol containing the given color.
 */
public record CardHasColorManaSymbolPredicate(ManaColor color) implements CardPredicate {
}
