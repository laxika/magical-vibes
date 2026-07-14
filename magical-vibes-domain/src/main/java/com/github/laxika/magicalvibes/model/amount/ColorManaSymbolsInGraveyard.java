package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma count across graveyard cards: the total number of {@code color} mana symbols in the mana
 * costs of the non-token cards in the graveyard(s) in scope. Hybrid and Phyrexian symbols of that
 * color each count once (see {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols});
 * generic and {@code X} symbols never count. Used by Umbra Stalker (black, {@link CountScope#CONTROLLER}).
 */
public record ColorManaSymbolsInGraveyard(ManaColor color, CountScope scope) implements DynamicAmount {
}
