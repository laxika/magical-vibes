package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Target player may search their own library for the named card and put it into their hand. */
public record SearchTargetPlayerLibraryForNamedCardToHandEffect(String cardName) implements CardEffect {

    public SearchTargetPlayerLibraryForNamedCardToHandEffect {
        Objects.requireNonNull(cardName, "cardName");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
