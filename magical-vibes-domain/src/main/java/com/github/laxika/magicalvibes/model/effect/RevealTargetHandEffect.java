package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player reveals their hand." The whole hand is revealed to all players (logged); nothing
 * further happens. Used by Thoughtcutter Agent, where it is paired with a {@link LoseLifeEffect} on
 * the same target player.
 */
public record RevealTargetHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
