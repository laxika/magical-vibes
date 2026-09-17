package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Offers each opponent the choice to pay for a token copy of the source permanent. */
public record ReflectEffect(
        String manaCost,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId
) implements CardEffect {

    public ReflectEffect(String manaCost) {
        this(manaCost, null, null, null);
    }

    public ReflectEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }
}
