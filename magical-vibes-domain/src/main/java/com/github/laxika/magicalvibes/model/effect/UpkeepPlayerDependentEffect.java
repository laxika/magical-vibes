package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability for an upkeep effect that triggers only during one particular player's upkeep.
 */
public interface UpkeepPlayerDependentEffect extends CardEffect {

    boolean triggersFor(UUID activePlayerId);
}
