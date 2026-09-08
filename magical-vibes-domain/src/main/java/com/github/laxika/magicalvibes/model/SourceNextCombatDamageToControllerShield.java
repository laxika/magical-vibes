package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One-shot replacement shield: the next time the chosen source would deal combat damage this turn,
 * that damage is dealt to the stored player instead.
 */
public record SourceNextCombatDamageToControllerShield(UUID sourcePermanentId, UUID controllerId) {
}
