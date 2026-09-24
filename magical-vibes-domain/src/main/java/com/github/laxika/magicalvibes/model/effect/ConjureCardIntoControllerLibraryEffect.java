package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.List;
import java.util.Map;

/** Conjures one card into its controller's library and adds the supplied perpetual effects. */
public record ConjureCardIntoControllerLibraryEffect(
        String setCode,
        String collectorNumber,
        Map<EffectSlot, List<CardEffect>> additionalEffects
) implements CardEffect {

    public ConjureCardIntoControllerLibraryEffect {
        if (additionalEffects == null) {
            throw new IllegalArgumentException("additionalEffects must not be null");
        }
        additionalEffects = additionalEffects.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));
    }
}
