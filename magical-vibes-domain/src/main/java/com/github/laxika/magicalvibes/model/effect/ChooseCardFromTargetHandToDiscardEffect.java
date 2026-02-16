package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public record ChooseCardFromTargetHandToDiscardEffect(int count, List<CardType> excludedTypes) implements CardEffect {
}
