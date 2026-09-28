package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Selects one of the supplied modal options uniformly at random. */
public record ChooseOneAtRandomEffect(List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {

    public ChooseOneAtRandomEffect {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("At least one random option is required");
        }
        if (options.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Random options must not contain null");
        }
        options = List.copyOf(options);
    }
}
