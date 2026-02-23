package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

public record DestroyAllPermanentsEffect(
        Set<CardType> targetTypes,
        boolean onlyOpponents,
        boolean cannotBeRegenerated,
        PermanentPredicate filter
) implements CardEffect {

    public DestroyAllPermanentsEffect {
        targetTypes = Set.copyOf(targetTypes);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes) {
        this(targetTypes, false, false, null);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean cannotBeRegenerated) {
        this(targetTypes, false, cannotBeRegenerated, null);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated) {
        this(targetTypes, onlyOpponents, cannotBeRegenerated, null);
    }
}
