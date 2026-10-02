package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Makes the permanent attached to the source Aura copy a source-linked exiled card until end of turn. */
public record EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect(
        Set<CardType> additionalTypesOverride) implements CardEffect {

    public EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect() {
        this(Set.of());
    }

    public EnchantedPermanentBecomesCopyOfExiledCardUntilEndOfTurnEffect {
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
    }
}
