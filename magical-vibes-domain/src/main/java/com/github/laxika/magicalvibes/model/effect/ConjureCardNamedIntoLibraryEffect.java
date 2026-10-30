package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Conjure … cards named … into your library": conjures copies of the given printing into the
 * resolving controller's library. Nothing is conjured while that printing is not implemented.
 */
public record ConjureCardNamedIntoLibraryEffect(
        String setCode,
        String collectorNumber,
        int count,
        Map<EffectSlot, List<CardEffect>> additionalEffects
) implements CardEffect {

    public ConjureCardNamedIntoLibraryEffect(String setCode, String collectorNumber, int count) {
        this(setCode, collectorNumber, count, Map.of());
    }

    public ConjureCardNamedIntoLibraryEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        if (additionalEffects == null) {
            throw new IllegalArgumentException("additionalEffects must not be null");
        }
        additionalEffects = additionalEffects.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));
    }
}
