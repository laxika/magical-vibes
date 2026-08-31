package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

import java.util.Set;

/** Trigger marker for a spell that copies itself when its optional casualty cost was paid. */
public record CopyThisSpellIfCasualtyPaidEffect(
        Set<CardSupertype> removedSupertypes,
        boolean tokenCopy,
        boolean startingLoyaltyFromX
) implements CardEffect {

    public CopyThisSpellIfCasualtyPaidEffect() {
        this(Set.of(), false, false);
    }

    public CopyThisSpellIfCasualtyPaidEffect {
        removedSupertypes = removedSupertypes == null ? Set.of() : Set.copyOf(removedSupertypes);
    }
}
