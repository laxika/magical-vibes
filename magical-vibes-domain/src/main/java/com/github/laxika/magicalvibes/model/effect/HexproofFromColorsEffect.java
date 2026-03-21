package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static effect granting "hexproof from [colors]".
 * The permanent can't be the target of spells or abilities of the specified colors
 * controlled by opponents. Unlike full hexproof, this only blocks sources that
 * match one of the specified colors.
 */
public record HexproofFromColorsEffect(Set<CardColor> colors) implements CardEffect {
}
