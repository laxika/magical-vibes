package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;

import java.util.List;

/**
 * "Conjure … cards named … into your library": conjures copies of the given printing into the
 * resolving controller's library. Nothing is conjured while that printing is not implemented.
 */
public record ConjureCardNamedIntoLibraryEffect(
        String setCode,
        String collectorNumber,
        int count,
        List<Ability> additionalAbilities
) implements CardEffect {

    public ConjureCardNamedIntoLibraryEffect(String setCode, String collectorNumber, int count) {
        this(setCode, collectorNumber, count, List.of());
    }

    public ConjureCardNamedIntoLibraryEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        if (additionalAbilities == null) {
            throw new IllegalArgumentException("additionalAbilities must not be null");
        }
        additionalAbilities = List.copyOf(additionalAbilities);
    }
}
