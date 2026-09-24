package com.github.laxika.magicalvibes.model.effect;

/** Marker for choosing one or more players as the permanent enters the battlefield. */
public record ChoosePlayerOnEnterEffect(int playerCount) implements ReplacementEffect {

    public ChoosePlayerOnEnterEffect() {
        this(1);
    }

    public ChoosePlayerOnEnterEffect {
        if (playerCount < 1) {
            throw new IllegalArgumentException("playerCount must be positive");
        }
    }
}
