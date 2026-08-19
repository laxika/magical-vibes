package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Static layer-4 effect that replaces a permanent's card types with the given types. */
public record SetCardTypesEffect(Set<CardType> cardTypes, GrantScope scope) implements CardEffect {

    public SetCardTypesEffect {
        cardTypes = Set.copyOf(cardTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
