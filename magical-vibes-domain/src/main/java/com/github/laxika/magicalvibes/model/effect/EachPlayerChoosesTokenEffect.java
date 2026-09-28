package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Each player chooses one token option and creates the chosen token under their control. */
public record EachPlayerChoosesTokenEffect(List<TokenOption> options) implements CardEffect {

    public EachPlayerChoosesTokenEffect {
        options = List.copyOf(options);
        if (options.size() < 2) {
            throw new IllegalArgumentException("At least two token options are required");
        }
    }

    public record TokenOption(String label, CreateTokenEffect token) {
    }
}
