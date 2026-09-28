package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/** Static effect: this creature can't attack its owner or planeswalkers its owner controls. */
public record CantAttackOwnerEffect() implements AttackerTargetRestrictionEffect {

    @Override
    public UUID restrictedPlayerId(Permanent sourcePermanent) {
        return sourcePermanent.getCard().getOwnerId();
    }

    @Override
    public boolean restrictsPlaneswalkers() {
        return true;
    }
}
