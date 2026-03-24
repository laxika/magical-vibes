package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public record ChooseCardFromTargetHandToExileEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                                     boolean returnOnSourceLeave) implements CardEffect {

    public ChooseCardFromTargetHandToExileEffect(int count, List<CardType> excludedTypes) {
        this(count, excludedTypes, List.of(), false);
    }

    public ChooseCardFromTargetHandToExileEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes) {
        this(count, excludedTypes, includedTypes, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
