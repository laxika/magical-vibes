package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker effect for one creature or land card offered by
 * {@link MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect}. The group ID keeps
 * separate resolutions from clearing one another's pending choices.
 */
public record PutMilledCreatureOrLandOnTopOfLibraryEffect(UUID groupId) implements CardEffect {
}
