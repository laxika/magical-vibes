package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public record ChooseCardFromTargetHandToDiscardEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes) implements CardEffect {

    public ChooseCardFromTargetHandToDiscardEffect(int count, List<CardType> excludedTypes) {
        this(count, excludedTypes, List.of());
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
