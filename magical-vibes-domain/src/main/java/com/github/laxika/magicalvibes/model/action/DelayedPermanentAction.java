package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Schedules one zone-change operation on one permanent at a fixed timing point (see
 * {@link DelayedPermanentActionKind}). Drained by
 * {@code PermanentRemovalService.processDelayedPermanentActions}; a permanent that already left
 * the battlefield by then is skipped. {@code cannotBeRegenerated} is honoured by the DESTROY
 * kinds only (e.g. "destroy it at end of combat, it can't be regenerated" triggers).
 */
public record DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                     boolean cannotBeRegenerated) implements DelayedAction {

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind) {
        this(permanentId, kind, false);
    }
}
