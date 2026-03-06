package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

public record AwardRestrictedManaEffect(ManaColor color, int amount, Set<CardType> allowedSpellTypes) implements CardEffect {
}
