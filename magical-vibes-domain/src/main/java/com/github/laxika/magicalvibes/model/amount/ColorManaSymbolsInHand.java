package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma count across the controller's hand: the total number of {@code color} mana symbols in the
 * mana costs of the cards in the controller's hand. Hybrid and Phyrexian symbols of that color each
 * count once (see {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic
 * and {@code X} symbols never count. Used by Phosphorescent Feast (green) — the "reveal any number of
 * cards" choice is modelled as the whole hand, since revealing more only ever raises the life gained.
 */
public record ColorManaSymbolsInHand(ManaColor color) implements DynamicAmount {
}
