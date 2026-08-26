package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that doubles life loss by opponents of the active player during that
 * player's turn.
 */
public record DoubleOpponentLifeLossEffect() implements OpponentLifeLossReplacementEffect {

    @Override
    public int lifeLossMultiplier() {
        return 2;
    }
}
