package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A resolved Words of War activation waiting to replace one draw.
 *
 * @param sourceCard the last-known card that deals the replacement damage
 * @param sourcePermanentId the source permanent id while it remains on the battlefield
 * @param targetId the target chosen when the ability was activated
 */
public record PendingNextDrawDamageReplacement(Card sourceCard,
                                               UUID sourcePermanentId,
                                               UUID targetId) {
}
