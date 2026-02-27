package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: return a dying creature card from the controller's graveyard
 * to the battlefield and attach the source equipment to it.
 * Used by Nim Deathmantle's "you may pay {4}" death trigger.
 *
 * @param dyingCardId the card ID of the dying creature (null in card definition, filled at trigger time)
 */
public record ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(UUID dyingCardId) implements CardEffect {

    public ReturnDyingCreatureToBattlefieldAndAttachSourceEffect() {
        this(null);
    }
}
