package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Makes the source permanent a copy of a creature card exiled with it until end of turn. */
public record BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(
        Set<CardType> additionalTypesOverride,
        Set<CardSubtype> additionalSubtypesOverride
) implements CardEffect {

    public BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect {
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
    }

    public BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect() {
        this(Set.of(), Set.of());
    }
}
