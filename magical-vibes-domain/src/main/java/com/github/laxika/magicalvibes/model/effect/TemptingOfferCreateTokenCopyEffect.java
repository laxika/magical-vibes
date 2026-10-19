package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Tempting offer that creates a copy for the spell's controller, then offers each opponent a
 * copy; an opponent who accepts also gives the spell's controller another copy. The accepting
 * opponents' copies and the controller's matching copies are only created once every opponent has
 * chosen, so {@code acceptedOpponentIds} accumulates the acceptances until then.
 */
public record TemptingOfferCreateTokenCopyEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID targetId,
        List<UUID> acceptedOpponentIds
) implements CardEffect {

    public TemptingOfferCreateTokenCopyEffect {
        Objects.requireNonNull(tokenCopyEffect, "tokenCopyEffect is required");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        acceptedOpponentIds = acceptedOpponentIds == null ? List.of() : List.copyOf(acceptedOpponentIds);
    }

    public TemptingOfferCreateTokenCopyEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect(), null, null, null, List.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
