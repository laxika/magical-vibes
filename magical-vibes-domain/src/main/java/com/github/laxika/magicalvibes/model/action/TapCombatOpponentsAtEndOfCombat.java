package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Creature whose blocked attackers are tapped and kept from untapping at the next end of combat.
 * The directional opponent set is read when the action is drained, so blocks made after the spell
 * resolves are included.
 */
public record TapCombatOpponentsAtEndOfCombat(UUID creatureId) implements DelayedAction {
}
