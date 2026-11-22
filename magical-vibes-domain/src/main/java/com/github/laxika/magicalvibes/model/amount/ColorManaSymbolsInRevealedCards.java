package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma count across the cards revealed earlier in this resolution, by the last reveal step ({@code StepKind.REVEAL},
 * Sanity Grinding) or else by the controller from their hand (via {@code RevealAnyNumberOfCardsFromHandEffect}): the
 * total number of {@code color} mana symbols
 * in the mana costs of those cards. Hybrid and Phyrexian symbols of that color each count once (see
 * {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic and {@code X}
 * symbols never count. Used by Phosphorescent Feast (green).
 */
public record ColorManaSymbolsInRevealedCards(ManaColor color) implements DynamicAmount {
}
