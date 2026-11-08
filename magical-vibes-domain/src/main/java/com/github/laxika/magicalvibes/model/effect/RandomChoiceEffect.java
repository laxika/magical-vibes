package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "Choose one at random —": one of several effect sequences, selected uniformly at random. A triggered ability's mode is
 * chosen as it goes on the stack (CR 603.3c, 700.2b; {@code StackedModalTriggerService}), so its stack entry and any
 * copy of it carry the chosen sequence; otherwise the sequence is chosen as the effect resolves.
 */
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
