package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Arrays;
import java.util.List;

/** As this permanent enters, its controller chooses the listed number of counter types. */
public record ChooseCounterTypeOnEnterEffect(List<CounterType> counterTypes, int choicesRequired)
        implements ReplacementEffect {

    public ChooseCounterTypeOnEnterEffect(CounterType... counterTypes) {
        this(1, counterTypes);
    }

    public ChooseCounterTypeOnEnterEffect(int choicesRequired, CounterType... counterTypes) {
        this(Arrays.asList(counterTypes), choicesRequired);
    }

    public ChooseCounterTypeOnEnterEffect(List<CounterType> counterTypes) {
        this(counterTypes, 1);
    }

    public ChooseCounterTypeOnEnterEffect {
        counterTypes = List.copyOf(counterTypes);
        if (counterTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one counter type is required");
        }
        if (choicesRequired < 1 || choicesRequired > counterTypes.stream().distinct().count()) {
            throw new IllegalArgumentException(
                    "The required choice count must be between one and the number of different counter types");
        }
    }
}
