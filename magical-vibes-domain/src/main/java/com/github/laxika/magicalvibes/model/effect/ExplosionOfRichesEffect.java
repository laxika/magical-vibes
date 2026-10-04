package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Draws for the controller, then offers one draw to each other player in turn order. */
public record ExplosionOfRichesEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        int drawnCards,
        List<UUID> acceptedOpponentIds
) implements CardEffect {

    public ExplosionOfRichesEffect() {
        this(null, null, 0, null);
    }

    public ExplosionOfRichesEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        if (acceptedOpponentIds != null) {
            acceptedOpponentIds = List.copyOf(acceptedOpponentIds);
        }
    }
}
