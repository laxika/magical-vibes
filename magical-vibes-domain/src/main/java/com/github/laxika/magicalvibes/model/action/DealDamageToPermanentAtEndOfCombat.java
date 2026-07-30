package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Permanent scheduled to be dealt {@code damage} damage when combat ends (Dwarven Sea Clan's "This
 * creature deals 2 damage to that creature at end of combat"). {@code sourcePermanentId} and
 * {@code sourceCard} identify the damage source; the card is kept so the delayed damage is still
 * dealt with last-known information when the source already left the battlefield. Drained in
 * {@code CombatService.processEndOfCombatDamage()}.
 */
public record DealDamageToPermanentAtEndOfCombat(UUID permanentId, UUID sourcePermanentId,
                                                 Card sourceCard, UUID controllerId,
                                                 int damage) implements DelayedAction {
}
