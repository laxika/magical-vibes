package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Destroys the predetermined permanent {@code linkedPermanentId} — "that creature" of a delayed
 * trigger registered by {@link RegisterDelayedDestroyTargetWhenSourceLeavesEffect}. Non-targeting: the
 * permanent is never chosen, and nothing happens if it has left the battlefield.
 *
 * @param cannotBeRegenerated whether the destroyed permanent can't be regenerated
 * @param linkedPermanentId   the permanent to destroy
 */
public record DestroyLinkedPermanentEffect(boolean cannotBeRegenerated, UUID linkedPermanentId)
        implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
