package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: "Whenever a creature blocks this turn, it gets +power/+toughness until end of
 * turn." Registered by Battle Cry. Fires once per unique blocker declared this turn. Cleared at
 * turn cleanup.
 */
public record DelayedBlockerBoost(UUID controllerId, int power, int toughness, Card sourceCard)
        implements DelayedAction {
}
