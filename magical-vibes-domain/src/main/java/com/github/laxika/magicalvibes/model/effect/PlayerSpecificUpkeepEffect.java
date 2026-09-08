package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * An each-upkeep effect that only triggers during the named player's upkeep.
 */
public interface PlayerSpecificUpkeepEffect extends CardEffect {

    UUID playerId();
}
