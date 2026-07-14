package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Chroma anthem: each creature its controller controls gets +{@code powerPerSymbol}/+{@code
 * toughnessPerSymbol} for each {@code manaColor} mana symbol in that creature's own mana cost
 * (Light from Within). Hybrid and Phyrexian symbols of that color count (see
 * {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}).
 */
public record BoostOwnCreaturesByManaSymbolEffect(
        ManaColor manaColor,
        int powerPerSymbol,
        int toughnessPerSymbol
) implements CardEffect {
}
