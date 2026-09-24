package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/** Capability for damage multipliers that use two players remembered by their source permanent. */
public interface ChosenPlayersDamageMultiplyingEffect extends DoublingEffect {

    int damageMultiplier();

    default boolean appliesTo(UUID sourceControllerId, UUID recipientPlayerId, Permanent effectSource) {
        if (sourceControllerId == null || recipientPlayerId == null
                || sourceControllerId.equals(recipientPlayerId)) {
            return false;
        }
        return effectSource.getChosenPlayerIds().contains(sourceControllerId)
                && effectSource.getChosenPlayerIds().contains(recipientPlayerId);
    }
}
