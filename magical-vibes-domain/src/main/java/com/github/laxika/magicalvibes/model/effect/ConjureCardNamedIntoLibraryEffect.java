package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Conjures the named card into the resolving controller's library. */
public record ConjureCardNamedIntoLibraryEffect(
        String cardName,
        int count,
        Map<EffectSlot, List<CardEffect>> additionalEffects
) implements CardEffect {

    public ConjureCardNamedIntoLibraryEffect(String cardName, int count) {
        this(cardName, count, Map.of());
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
