package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;

import java.util.List;

/** Conjures one card into its controller's library and adds the supplied perpetual effects. */
public record ConjureCardIntoControllerLibraryEffect(
        String setCode,
        String collectorNumber,
        List<Ability> additionalAbilities
) implements CardEffect {

    public ConjureCardIntoControllerLibraryEffect {
        if (additionalAbilities == null) {
            throw new IllegalArgumentException("additionalAbilities must not be null");
        }
        additionalAbilities = List.copyOf(additionalAbilities);
    }
}
