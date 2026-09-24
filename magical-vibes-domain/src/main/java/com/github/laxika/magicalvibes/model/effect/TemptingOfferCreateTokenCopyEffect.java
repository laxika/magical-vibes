package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Tempting offer that creates a copy for the spell's controller, then offers each opponent a
 * copy; an opponent who accepts also gives the spell's controller another copy.
 */
public record TemptingOfferCreateTokenCopyEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID targetId
) implements CardEffect {

    public TemptingOfferCreateTokenCopyEffect {
        Objects.requireNonNull(tokenCopyEffect, "tokenCopyEffect is required");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferCreateTokenCopyEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect(), null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
