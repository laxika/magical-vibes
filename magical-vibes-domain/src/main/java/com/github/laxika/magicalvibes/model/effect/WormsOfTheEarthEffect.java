package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Resolves Worms of the Earth's upkeep choice one player at a time in APNAP order. */
public record WormsOfTheEarthEffect(
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean damageChoice
) implements CardEffect {

    public WormsOfTheEarthEffect() {
        this(null, null, null, false);
    }
}
