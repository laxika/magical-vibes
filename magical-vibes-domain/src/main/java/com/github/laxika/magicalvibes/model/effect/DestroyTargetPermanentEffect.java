package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

public record DestroyTargetPermanentEffect(Set<CardType> targetTypes, boolean cannotBeRegenerated) implements CardEffect {

    public DestroyTargetPermanentEffect(Set<CardType> targetTypes) {
        this(targetTypes, false);
    }
}
