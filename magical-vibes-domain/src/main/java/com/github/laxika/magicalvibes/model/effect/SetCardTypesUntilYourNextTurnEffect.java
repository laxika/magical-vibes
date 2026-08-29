package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Replaces a permanent's card types until its controller's next turn. */
public record SetCardTypesUntilYourNextTurnEffect(Set<CardType> cardTypes) implements CardEffect {

    public SetCardTypesUntilYourNextTurnEffect {
        cardTypes = Set.copyOf(cardTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
