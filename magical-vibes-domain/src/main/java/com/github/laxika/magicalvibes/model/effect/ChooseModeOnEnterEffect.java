package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Marker effect for a permanent that stores a mode choice as it enters the battlefield.
 */
public record ChooseModeOnEnterEffect(List<String> modes) implements CardEffect {

    public ChooseModeOnEnterEffect {
        modes = modes == null ? List.of() : List.copyOf(modes);
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("modes must not be empty");
        }
    }
}
