package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Resolves one of several effect sequences, selected uniformly at random. */
public record RandomChoiceEffect(List<List<CardEffect>> options) implements CardEffect {

    public RandomChoiceEffect {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("At least one random choice option is required");
        }
        options = options.stream().map(option -> {
            if (option == null || option.isEmpty()) {
                throw new IllegalArgumentException("Random choice options must contain at least one effect");
            }
            return List.copyOf(option);
        }).toList();
    }
}
