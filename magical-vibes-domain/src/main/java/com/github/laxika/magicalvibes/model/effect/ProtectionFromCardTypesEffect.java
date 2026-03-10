package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record ProtectionFromCardTypesEffect(Set<CardType> cardTypes) implements CardEffect {
}
