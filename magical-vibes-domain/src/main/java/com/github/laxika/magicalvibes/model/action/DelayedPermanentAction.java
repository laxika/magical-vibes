package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Schedules one zone-change operation on one permanent at a fixed timing point (see
 * {@link DelayedPermanentActionKind}). Drained by
 * {@code PermanentRemovalService.processDelayedPermanentActions}; a permanent that already left
 * the battlefield by then is skipped. A non-null {@code controllerId} restricts an end-step action
 * to that player's end step. When {@code followsPermanentController} is true, the permanent's
 * current controller determines that end step instead (Aethermage's Touch).
 * {@code cannotBeRegenerated} is honoured by the DESTROY kinds only
 * (e.g. "destroy it at end of combat, it can't be regenerated" triggers).
 * A non-null {@code sourceCard} is the card whose ability created the delayed trigger (a spell
 * that creates one is its source); when null the permanent's own card is used as the source.
 * A non-null {@code triggerControllerId} is the controller of the delayed trigger itself (the
 * controller of the ability that created it), and a non-null {@code triggerSourcePermanentId} is the
 * permanent that ability came from; both default to the scheduled permanent's own data when null.
 * A {@code recurring} action models a granted "at the beginning of the end step" ability that stays
 * on the permanent: it is scheduled again each time it is drained while the permanent is still on
 * the battlefield, so a countered trigger fires again at the next end step.
 */
public record DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                     boolean cannotBeRegenerated, UUID returnExiledCardId,
                                     UUID controllerId, UUID sacrificingPlayerId,
                                     boolean followsPermanentController, Card sourceCard,
                                     UUID triggerControllerId, UUID triggerSourcePermanentId,
                                     boolean recurring)
        implements DelayedAction {

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId,
                                  UUID controllerId, UUID sacrificingPlayerId,
                                  boolean followsPermanentController, Card sourceCard,
                                  UUID triggerControllerId, UUID triggerSourcePermanentId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, controllerId, sacrificingPlayerId,
                followsPermanentController, sourceCard, triggerControllerId, triggerSourcePermanentId, false);
    }

    /** Creates a recurring action; see the class description. */
    public static DelayedPermanentAction recurring(UUID permanentId, DelayedPermanentActionKind kind) {
        return new DelayedPermanentAction(permanentId, kind, false, null, null, null, false, null, null, null, true);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId,
                                  UUID controllerId, UUID sacrificingPlayerId,
                                  boolean followsPermanentController, Card sourceCard) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, controllerId, sacrificingPlayerId,
                followsPermanentController, sourceCard, null, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId,
                                  UUID controllerId, UUID sacrificingPlayerId,
                                  boolean followsPermanentController) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId,
                controllerId, sacrificingPlayerId, followsPermanentController, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId,
                                  UUID controllerId, UUID sacrificingPlayerId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId,
                controllerId, sacrificingPlayerId, false);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId,
                                  UUID controllerId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, controllerId, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated, UUID returnExiledCardId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, null, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind,
                                  boolean cannotBeRegenerated) {
        this(permanentId, kind, cannotBeRegenerated, null, null, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind) {
        this(permanentId, kind, false, null, null, null);
    }

    /** Creates an end-step action that is drained only during the controller's end step. */
    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, UUID controllerId) {
        this(permanentId, kind, false, null, controllerId, null);
    }
}
