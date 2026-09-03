package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Offers each player the option to sacrifice a fixed number of creatures, then sacrifices the
 * source if a player accepts.
 *
 * @param count number of creatures the accepting player sacrifices
 * @param remainingPlayerIds players still to receive the choice
 * @param abilityControllerId controller of the triggered ability
 * @param sourcePermanentId the source permanent to sacrifice after an acceptance
 */
public record AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
        int count,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID sourcePermanentId
) implements CardEffect {

    public AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(int count) {
        this(count, null, null, null);
    }
}
