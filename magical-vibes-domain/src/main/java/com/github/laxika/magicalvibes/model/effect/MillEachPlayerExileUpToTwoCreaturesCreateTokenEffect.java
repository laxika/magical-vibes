package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Mills each player, then exiles up to two creature cards milled into graveyards and creates an X/X token. */
public record MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect(
        int count,
        CreateTokenEffect tokenTemplate
) implements CardEffect {

    public MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
    }
}
