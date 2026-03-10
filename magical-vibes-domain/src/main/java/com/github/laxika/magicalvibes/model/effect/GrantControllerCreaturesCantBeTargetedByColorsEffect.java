package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * When resolved, grants the controller's creatures protection from being targeted
 * by spells of the specified colors until end of turn.
 * Used by Autumn's Veil and similar cards.
 */
public record GrantControllerCreaturesCantBeTargetedByColorsEffect(Set<CardColor> colors) implements CardEffect {
}
