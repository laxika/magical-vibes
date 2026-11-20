package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Layer-4 effect that replaces a permanent's card types with the given types. */
public record SetCardTypesEffect(Set<CardType> cardTypes, PermanentSubject subject, Duration duration)
        implements CardEffect {

    public SetCardTypesEffect {
        cardTypes = Set.copyOf(cardTypes);
    }

    public SetCardTypesEffect(Set<CardType> cardTypes, PermanentSubject subject) {
        this(cardTypes, subject, Duration.PERMANENT);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
