package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Capability for a static effect on a creature that removes specific players, and optionally
 * their planeswalkers, from that creature's legal attack targets.
 */
public interface AttackerTargetRestrictionEffect extends CardEffect {

    /** The player whose player target is restricted, derived from the source permanent. */
    UUID restrictedPlayerId(Permanent sourcePermanent);

    /** Whether planeswalkers controlled by the restricted player are restricted too. */
    default boolean restrictsPlaneswalkers() {
        return false;
    }
}
