package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * When resolved, grants the controller's spells protection from being countered
 * by spells of the specified colors until end of turn.
 * Used by Autumn's Veil and similar cards.
 */
public record GrantControllerSpellsCantBeCounteredByColorsEffect(Set<CardColor> colors) implements CardEffect {
}
