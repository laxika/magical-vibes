package com.github.laxika.magicalvibes.model.effect;

/** Plays the card exiled as this ability's cost, during resolution or until end of turn. */
public record AllowPlayExiledCostCardThisTurnEffect(boolean duringResolution) implements CardEffect {
    public AllowPlayExiledCostCardThisTurnEffect() {
        this(false);
    }
}
