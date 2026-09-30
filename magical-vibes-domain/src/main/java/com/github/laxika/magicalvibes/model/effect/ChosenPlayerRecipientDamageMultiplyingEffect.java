package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/** Capability for damage multipliers that use one player remembered by their source permanent. */
public interface ChosenPlayerRecipientDamageMultiplyingEffect extends DoublingEffect {

    int damageMultiplier();

    default boolean appliesTo(UUID recipientPlayerId, Permanent effectSource) {
        return recipientPlayerId != null
                && recipientPlayerId.equals(effectSource.getRememberedTargetPlayerId());
    }
}
