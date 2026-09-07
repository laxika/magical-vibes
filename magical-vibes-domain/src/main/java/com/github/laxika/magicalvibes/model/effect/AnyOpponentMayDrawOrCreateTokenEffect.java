package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Each opponent may have the controller draw a card; if none do, the controller creates the supplied token. */
public record AnyOpponentMayDrawOrCreateTokenEffect(
        CreateTokenEffect tokenEffect,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        boolean anyAccepted
) implements CardEffect {

    public AnyOpponentMayDrawOrCreateTokenEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, null, null, false);
    }

    public AnyOpponentMayDrawOrCreateTokenEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }
}
