package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Pending-choice marker for a card offered from a targeted player's sideboard.
 */
public record MayCastFromSideboardWithoutPayingManaCostEffect(UUID sideboardOwnerId)
        implements CardEffect {
}
