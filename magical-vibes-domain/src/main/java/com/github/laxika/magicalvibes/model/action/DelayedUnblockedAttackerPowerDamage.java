package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: "Until end of turn, whenever a creature you control attacks and isn't blocked,
 * you may have it deal damage equal to its power to a target creature. If you do, it assigns no
 * combat damage this turn." Registered by Gaze of Pain. Fires once per unblocked attacker the
 * controller controls. Cleared at turn cleanup.
 */
public record DelayedUnblockedAttackerPowerDamage(UUID controllerId, Card sourceCard)
        implements DelayedAction {
}
