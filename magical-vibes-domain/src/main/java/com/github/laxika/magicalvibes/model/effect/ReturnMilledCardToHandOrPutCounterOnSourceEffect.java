package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker for one grouped offer created by
 * {@link MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect}.
 */
public record ReturnMilledCardToHandOrPutCounterOnSourceEffect(UUID groupId) implements CardEffect {
}
