package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: "Whenever a creature attacks this turn, it gets +power/+toughness until end of
 * turn." Registered by Song of Blood after milling. Fires once per attacking creature declared.
 * Cleared at turn cleanup.
 */
public record DelayedAttackerBoost(UUID controllerId, int power, int toughness, Card sourceCard)
        implements DelayedAction {
}
