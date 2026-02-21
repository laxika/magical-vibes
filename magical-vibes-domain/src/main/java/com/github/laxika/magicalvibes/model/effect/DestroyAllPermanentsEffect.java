package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record DestroyAllPermanentsEffect(
        Set<CardType> targetTypes,
        boolean onlyOpponents,
        boolean cannotBeRegenerated
) implements CardEffect {

    public DestroyAllPermanentsEffect {
        targetTypes = Set.copyOf(targetTypes);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes) {
        this(targetTypes, false, false);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean cannotBeRegenerated) {
        this(targetTypes, false, cannotBeRegenerated);
    }
}
