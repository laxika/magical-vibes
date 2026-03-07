package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record SearchTargetLibraryForCardsToGraveyardEffect(
        int maxCount,
        Set<CardType> cardTypes
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
