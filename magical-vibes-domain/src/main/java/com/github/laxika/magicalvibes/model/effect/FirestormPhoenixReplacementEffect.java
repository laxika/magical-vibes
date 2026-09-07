package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Static replacement for Firestorm Phoenix's self-replacement when it would die.
 */
public record FirestormPhoenixReplacementEffect() implements DyingCreatureReturnToHandReplacementEffect {

    @Override
    public boolean appliesTo(Permanent source, Permanent dyingCreature, boolean dyingCreatureIsEnchanted,
                             UUID sourceControllerId, UUID dyingCreatureControllerId) {
        return source.getId().equals(dyingCreature.getId());
    }

    @Override
    public boolean revealsReturnedCardUntilOwnerNextTurn() {
        return true;
    }

    @Override
    public boolean preventsPlayingReturnedCardUntilOwnerNextTurn() {
        return true;
    }
}
