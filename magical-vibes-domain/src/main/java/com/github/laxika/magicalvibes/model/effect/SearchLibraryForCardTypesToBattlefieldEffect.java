package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record SearchLibraryForCardTypesToBattlefieldEffect(Set<CardType> cardTypes, boolean requiresBasicSupertype,
                                                           boolean entersTapped) implements CardEffect {
}
