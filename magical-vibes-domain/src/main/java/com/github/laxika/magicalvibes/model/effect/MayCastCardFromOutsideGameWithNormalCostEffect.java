package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Internal pending-choice marker for a normal-cost cast from outside the game. */
public record MayCastCardFromOutsideGameWithNormalCostEffect(UUID offerGroupId) implements CardEffect {
}
