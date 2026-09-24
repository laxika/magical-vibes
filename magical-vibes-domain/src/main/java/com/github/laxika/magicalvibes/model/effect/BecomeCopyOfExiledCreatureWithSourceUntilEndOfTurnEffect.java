package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Makes the source permanent a copy of a creature card exiled with it until end of turn. */
public record BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(
        Set<CardType> additionalTypes,
        Set<CardSubtype> additionalSubtypes
) implements CardEffect {

    public BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect {
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
        additionalSubtypes = additionalSubtypes == null ? Set.of() : Set.copyOf(additionalSubtypes);
    }

    public BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect() {
        this(Set.of(), Set.of());
    }
}
