package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static effect: controller can't cast spells of the specified types.
 */
public record CantCastSpellTypeEffect(Set<CardType> restrictedTypes) implements CardEffect {
}
