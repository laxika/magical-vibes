package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma count across the cards the controller revealed from their hand earlier in this resolution
 * (via {@code RevealAnyNumberOfCardsFromHandEffect}): the total number of {@code color} mana symbols
 * in the mana costs of those cards. Hybrid and Phyrexian symbols of that color each count once (see
 * {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic and {@code X}
 * symbols never count. Used by Phosphorescent Feast (green).
 */
public record ColorManaSymbolsInRevealedCards(ManaColor color) implements DynamicAmount {
}
