package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that lets the activator choose an opponent to gain control of the source permanent. */
public record DelayedChooseOpponentGainsControlOfSource(
        UUID controllerId, UUID sourcePermanentId, Card sourceCard) implements DelayedAction {
}
