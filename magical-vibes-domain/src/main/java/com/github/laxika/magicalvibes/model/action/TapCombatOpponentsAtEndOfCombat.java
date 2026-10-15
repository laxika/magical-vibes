package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/**
 * Triton Tactics' delayed "at this turn's next end of combat" trigger: taps each creature blocked by
 * one of {@code creatureIds} and keeps it from untapping. Drained in
 * {@code CombatService.processEndOfCombatCombatOpponentTaps()}, which puts the trigger from
 * {@code sourceCard} on the stack under {@code controllerId}; the directional opponent set is read
 * as that trigger resolves, so blocks made after the spell resolved are included. Discarded at
 * cleanup when no end of combat came this turn.
 */
public record TapCombatOpponentsAtEndOfCombat(List<UUID> creatureIds, Card sourceCard, UUID controllerId)
        implements DelayedAction {
}
