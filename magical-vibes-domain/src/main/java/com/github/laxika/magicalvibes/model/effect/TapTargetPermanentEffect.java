package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record TapTargetPermanentEffect(Set<CardType> allowedTypes) implements CardEffect {
}
