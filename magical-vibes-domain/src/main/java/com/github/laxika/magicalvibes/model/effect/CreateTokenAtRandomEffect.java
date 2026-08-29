package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Creates one token using one of the supplied token blueprints, selected uniformly at random. */
public record CreateTokenAtRandomEffect(List<CreateTokenEffect> tokenOptions) implements CardEffect {

    public CreateTokenAtRandomEffect {
        if (tokenOptions == null || tokenOptions.isEmpty()) {
            throw new IllegalArgumentException("At least one token option is required");
        }
        tokenOptions = List.copyOf(tokenOptions);
    }
}
