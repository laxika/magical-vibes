package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger that destroys each creature that deals combat damage to the watched planeswalker
 * until its controller's next turn.
 */
public record DelayedDestroyCreatureDealingCombatDamageToPlaneswalker(
        UUID planeswalkerId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
