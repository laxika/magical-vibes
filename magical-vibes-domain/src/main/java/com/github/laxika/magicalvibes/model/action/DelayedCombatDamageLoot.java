package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed trigger: "Whenever one or more creatures you control deal combat damage to a player this turn,
 *  draw N, then discard N." Registered by Jace, Cunning Castaway's +1. Cleared at start of new turn. */
public record DelayedCombatDamageLoot(UUID controllerId, int drawAmount, int discardAmount, Card sourceCard) implements DelayedAction {
}
