package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** One-shot delayed trigger for a Vehicle that was crewed earlier in the turn. */
public record DelayedVehicleAttack(
        UUID controllerId,
        UUID sourcePermanentId,
        UUID vehicleId,
        Card sourceCard,
        CardEffect effect
) implements DelayedAction {
}
