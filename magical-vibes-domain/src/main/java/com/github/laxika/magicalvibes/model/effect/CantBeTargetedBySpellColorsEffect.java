package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

public record CantBeTargetedBySpellColorsEffect(Set<CardColor> colors) implements CardEffect {
}
