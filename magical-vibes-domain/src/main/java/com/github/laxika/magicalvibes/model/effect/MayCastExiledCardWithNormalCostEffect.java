package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Internal pending-choice marker for a normal-cost spell cast from source-linked exile. */
public record MayCastExiledCardWithNormalCostEffect(UUID offerGroupId) implements CardEffect {
}
