package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Replaces a permanent's card types until the end of the turn. */
public record SetCardTypesUntilEndOfTurnEffect(Set<CardType> cardTypes, GrantScope scope)
        implements CardEffect {

    public SetCardTypesUntilEndOfTurnEffect {
        cardTypes = Set.copyOf(cardTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case SELF -> new TargetSpec(null, false, null, true, 1);
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
