package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Whole-turn replacement shield for combat damage from one source to a protected player.
 * The damage is dealt to the source's controller instead.
 */
public record TurnSourceDamageRedirectToControllerShield(UUID protectedPlayerId,
                                                          UUID sourcePermanentId) {
}
