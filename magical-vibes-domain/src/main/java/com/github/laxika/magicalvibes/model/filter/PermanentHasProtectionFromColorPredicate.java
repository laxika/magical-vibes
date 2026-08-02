package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * The permanent has protection from the given color (Escaped Shapeshifter's "protection from
 * any color", which is checked once per color).
 */
public record PermanentHasProtectionFromColorPredicate(CardColor color) implements PermanentPredicate {
}
