package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** A finite-use triggered ability granted directly to a player. */
public record Boon(UUID controllerId, Card sourceCard, CardEffect effect, int remainingUses) {

    public Boon {
        if (remainingUses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one remaining use");
        }
    }
}
