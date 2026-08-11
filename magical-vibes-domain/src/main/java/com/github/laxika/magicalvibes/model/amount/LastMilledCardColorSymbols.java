package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/** The number of symbols of a color in the mana cost of the most recently milled card. */
public record LastMilledCardColorSymbols(ManaColor color) implements DynamicAmount {
}
