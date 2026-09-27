package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Modifies the generic mana portion of one permanent's cost to turn face up. */
public record ReduceTurnFaceUpCostForPermanentEffect(UUID permanentId, int amount) implements CardEffect {
}
