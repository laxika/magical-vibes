package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Resolves exactly one of the supplied effects, selected uniformly at random. */
public record RandomChoiceEffect(List<CardEffect> options) implements CardEffect {

    public RandomChoiceEffect {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("At least one random-choice option is required");
        }
        options = List.copyOf(options);
    }
}
