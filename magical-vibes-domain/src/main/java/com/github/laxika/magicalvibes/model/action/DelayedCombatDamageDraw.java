package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger for drawing whenever a creature the controller controls deals combat damage. */
public record DelayedCombatDamageDraw(UUID controllerId, Card sourceCard) implements DelayedAction {
}
