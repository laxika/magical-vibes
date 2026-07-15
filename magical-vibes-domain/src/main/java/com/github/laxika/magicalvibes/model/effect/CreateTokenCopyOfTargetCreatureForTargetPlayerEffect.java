package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player creates a token that's a copy of target creature you control.
 * Reads {@code targetIds[0]} as the player and {@code targetIds[1]} as the creature to copy.
 */
public record CreateTokenCopyOfTargetCreatureForTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
