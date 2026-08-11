package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Until end of turn, the controller and permanents they control have hexproof from the given
 * colors.
 */
public record GrantControllerAndPermanentsHexproofFromColorsEffect(Set<CardColor> colors)
        implements CardEffect {
}
