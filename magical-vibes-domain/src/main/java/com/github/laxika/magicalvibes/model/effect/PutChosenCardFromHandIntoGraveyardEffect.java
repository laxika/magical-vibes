package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/** Lets the controller choose one of the specified hand cards to put into their graveyard. */
public record PutChosenCardFromHandIntoGraveyardEffect(Set<UUID> validCardIds) implements CardEffect {

    public PutChosenCardFromHandIntoGraveyardEffect {
        validCardIds = Set.copyOf(validCardIds);
    }
}
