package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Count of {@code color} mana symbols in the mana costs of all permanents the controller controls.
 * Hybrid and Phyrexian symbols of that color each count once (see
 * {@link com.github.laxika.magicalvibes.model.ManaCost#countColorSymbols}); generic and {@code X}
 * symbols never count.
 *
 * <p>When {@code devotion} is {@code true} (the default constructor) the count is the controller's
 * devotion to {@code color}, so devotion modifiers such as Altar of the Pantheon's are added on top
 * of the printed symbols. When {@code devotion} is {@code false} it is a chroma count (Springjack
 * Shepherd, Heartlash Cinder, Outrage Shaman, Primalcrux), which counts only the mana symbols.
 */
public record ColorManaSymbolsAmongControlledPermanents(ManaColor color, boolean devotion) implements DynamicAmount {

    public ColorManaSymbolsAmongControlledPermanents(ManaColor color) {
        this(color, true);
    }
}
