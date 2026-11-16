package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * One zone-change operation on one permanent a delayed triggered ability performs when its step begins (see
 * {@link DelayedPermanentActionKind}); it is the payload of a {@code ResolveDelayedPermanentActionEffect}, one step of
 * the ability, and the ability does nothing to a permanent that left the battlefield by then (CR 603.7c).
 * {@code cannotBeRegenerated} is honoured by the DESTROY kinds only (e.g. "destroy it at end of combat, it can't be
 * regenerated"). {@code returnExiledCardId} is the card a sacrificed permanent gives back from exile, returned to the
 * battlefield once the permanent was sacrificed. A sacrifice is made by the ability's controller, so it does nothing to
 * a permanent that player no longer controls, unless {@code currentControllerSacrifices}: then the permanent's
 * controller sacrifices it, whoever that is.
 * {@code schedulingResolution} is the identity of the resolution that scheduled the action
 * ({@code GameData.resolutionOrdinal}; 0 for none): the actions one resolution schedules together are one delayed
 * triggered ability ("exile them at the beginning of the next end step"), and those another resolution of the same
 * source schedules are another (CR 603.7a).
 */
public record DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, boolean cannotBeRegenerated,
                                     UUID returnExiledCardId, boolean currentControllerSacrifices,
                                     long schedulingResolution) {

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, boolean cannotBeRegenerated,
                                  UUID returnExiledCardId, boolean currentControllerSacrifices) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, currentControllerSacrifices, 0);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, boolean cannotBeRegenerated,
                                  UUID returnExiledCardId) {
        this(permanentId, kind, cannotBeRegenerated, returnExiledCardId, false);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind, boolean cannotBeRegenerated) {
        this(permanentId, kind, cannotBeRegenerated, null);
    }

    public DelayedPermanentAction(UUID permanentId, DelayedPermanentActionKind kind) {
        this(permanentId, kind, false);
    }

    /** This action as scheduled by the resolution {@code resolution}. */
    public DelayedPermanentAction scheduledBy(long resolution) {
        return new DelayedPermanentAction(permanentId, kind, cannotBeRegenerated, returnExiledCardId,
                currentControllerSacrifices, resolution);
    }
}
