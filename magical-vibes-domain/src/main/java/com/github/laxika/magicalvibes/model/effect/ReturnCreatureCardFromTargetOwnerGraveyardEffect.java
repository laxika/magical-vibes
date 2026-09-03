package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Resolves Reincarnation's delayed effect by letting the delayed ability's controller choose a
 * creature card from the targeted creature's owner's graveyard and returning it under that owner's
 * control.
 *
 * <p>The graveyard owner is bound when the target is registered, before the target leaves the
 * battlefield. The unbound form is used inside {@link ResolveEffectOnTargetDeathThisTurnEffect}.
 */
public record ReturnCreatureCardFromTargetOwnerGraveyardEffect(UUID graveyardOwnerId) implements CardEffect {

    public ReturnCreatureCardFromTargetOwnerGraveyardEffect() {
        this(null);
    }

    public ReturnCreatureCardFromTargetOwnerGraveyardEffect bindOwner(UUID ownerId) {
        return new ReturnCreatureCardFromTargetOwnerGraveyardEffect(ownerId);
    }
}
