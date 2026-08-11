package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker effect for one permanent card offered by
 * {@link MillControllerAndMayReturnMilledPermanentToHandEffect}. The group ID keeps separate
 * resolutions from clearing one another's pending choices.
 */
public record ReturnMilledPermanentToHandEffect(UUID groupId) implements CardEffect {
}
