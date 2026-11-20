package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Replaces a permanent's card types until the end of the turn. */
public record SetCardTypesUntilEndOfTurnEffect(Set<CardType> cardTypes, PermanentSubject subject)
        implements CardEffect {

    public SetCardTypesUntilEndOfTurnEffect {
        cardTypes = Set.copyOf(cardTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
