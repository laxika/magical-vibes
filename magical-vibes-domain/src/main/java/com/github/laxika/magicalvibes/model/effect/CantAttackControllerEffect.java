package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/** Static effect: this creature can't attack its current controller. */
public record CantAttackControllerEffect() implements AttackerTargetRestrictionEffect {

    @Override
    public UUID restrictedPlayerId(Permanent sourcePermanent) {
        return null;
    }

    @Override
    public boolean restrictsSourceController() {
        return true;
    }
}
