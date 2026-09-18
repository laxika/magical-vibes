package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that deals damage equal to the number of attacking creatures this turn. */
public record DelayedAttackDamage(
        UUID controllerId,
        UUID targetPermanentId,
        UUID sourcePermanentId,
        Card sourceCard
) implements DelayedAction {
}
