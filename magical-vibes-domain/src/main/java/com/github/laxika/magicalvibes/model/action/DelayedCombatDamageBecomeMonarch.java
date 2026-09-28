package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that makes the controller the monarch after qualifying combat damage. */
public record DelayedCombatDamageBecomeMonarch(UUID controllerId, Card sourceCard)
        implements DelayedAction {
}
