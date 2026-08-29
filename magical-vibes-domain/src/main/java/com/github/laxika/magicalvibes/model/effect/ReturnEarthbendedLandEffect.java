package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Returns the land card left behind by an Earthbend action under that action's controller. */
public record ReturnEarthbendedLandEffect(UUID returnControllerId, boolean fromExile)
        implements CardEffect {
}
