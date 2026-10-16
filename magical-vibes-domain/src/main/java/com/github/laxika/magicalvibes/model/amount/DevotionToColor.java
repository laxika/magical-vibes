package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * The controller's devotion to the given color: the color mana symbols in the mana costs of the
 * permanents they control, plus any devotion increases (Altar of the Pantheon).
 */
public record DevotionToColor(ManaColor color) implements DynamicAmount {
}
