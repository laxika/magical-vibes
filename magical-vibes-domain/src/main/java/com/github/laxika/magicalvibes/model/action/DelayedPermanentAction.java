package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Schedules one zone-change operation on one permanent at a fixed timing point (see
 * {@link DelayedPermanentActionKind}). Drained by
 * {@code PermanentRemovalService.processDelayedPermanentActions}; a permanent that already left
 * the battlefield by then is skipped. A non-null {@code controllerId} restricts an end-step action
 * to that player's end step. {@code cannotBeRegenerated} is honoured by the DESTROY kinds only
 * (e.g. "destroy it at end of combat, it can't be regenerated" triggers).
 */
public record DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                     boolean cannotBeRegenerated, UUID returnExiledCardId,
                                     UUID controllerId) implements DelayedAction {

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated) {
        this(permanentId, kind, cannotBeRegenerated, null, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind) {
        this(permanentId, kind, false, null, null);
    }

    /** Creates an end-step action that is drained only during the controller's end step. */
    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, UUID controllerId) {
        this(permanentId, kind, false, null, controllerId);
    }
}
