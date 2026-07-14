package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma count across permanents: the total number of {@code color} mana symbols in the mana costs
 * of all permanents the controller controls. Hybrid and Phyrexian symbols of that color each count
 * once (see {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic and
 * {@code X} symbols never count. Used by Springjack Shepherd (white).
 */
public record ColorManaSymbolsAmongControlledPermanents(ManaColor color) implements DynamicAmount {
}
