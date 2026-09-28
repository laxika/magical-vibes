package com.github.laxika.magicalvibes.model.effect;

/** Sets the controller's maximum life total for the rest of the game. */
public record SetMaximumLifeTotalEffect(int maximumLifeTotal) implements CardEffect {

    public SetMaximumLifeTotalEffect {
        if (maximumLifeTotal < 0) {
            throw new IllegalArgumentException("Maximum life total cannot be negative");
        }
    }
}
