package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Creates a token copy of the permanent attached to the source Aura. */
public record CreateTokenCopyOfEnchantedPermanentEffect(List<CardSubtype> additionalSubtypes)
        implements CardEffect {

    public CreateTokenCopyOfEnchantedPermanentEffect() {
        this(List.of());
    }
}
