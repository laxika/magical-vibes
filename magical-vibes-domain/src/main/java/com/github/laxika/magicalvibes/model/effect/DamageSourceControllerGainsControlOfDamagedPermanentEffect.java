package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "That source's controller gains control of this creature" (Crag Saurian, Contested War Zone): the controller of the
 * stack entry's triggering permanent as the ability resolves, or its last known controller (CR 608.2h), or the
 * controller the entry recorded for a source that was never a permanent.
 *
 * @param newControllerId the player a collector names instead, when the new controller isn't the source's
 *                        (Contested Game Ball's "the attacking player"); {@code null} on cards
 */
public record DamageSourceControllerGainsControlOfDamagedPermanentEffect(UUID newControllerId) implements CardEffect {

    public DamageSourceControllerGainsControlOfDamagedPermanentEffect() {
        this(null);
    }
}
