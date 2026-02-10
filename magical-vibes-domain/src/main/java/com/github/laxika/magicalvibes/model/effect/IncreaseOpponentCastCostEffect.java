package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record IncreaseOpponentCastCostEffect(Set<CardType> affectedTypes, int amount) implements CardEffect {
}
