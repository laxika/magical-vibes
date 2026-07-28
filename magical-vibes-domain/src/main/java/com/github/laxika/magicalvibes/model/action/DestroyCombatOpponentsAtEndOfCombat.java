package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Creature whose combat opponents are destroyed when combat ends: "At this turn's next end of
 * combat, destroy all creatures that blocked or were blocked by it this turn" (Venomous Breath).
 * The opponent set is resolved at drain time from
 * {@code GameData.combatBlockOpponentIdsThisTurn}, so blocks declared after the spell resolved are
 * included. Drained in {@code CombatService.processEndOfCombatCombatOpponentDestructions()}.
 */
public record DestroyCombatOpponentsAtEndOfCombat(UUID creatureId) implements DelayedAction {
}
