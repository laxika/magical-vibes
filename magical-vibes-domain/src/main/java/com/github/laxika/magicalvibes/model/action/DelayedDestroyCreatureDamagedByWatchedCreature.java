package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger: "Whenever that creature deals combat damage to a non-Wall creature this turn,
 * destroy that non-Wall creature." Registered by Acidic Dagger's activated ability. Fires once per
 * damaged non-Wall creature, in every combat damage step of the turn. Cleared at turn cleanup.
 *
 * @param watchedPermanentId the targeted creature whose combat damage is watched
 * @param controllerId       controller of the delayed trigger (the ability's controller)
 * @param sourceCard         card used for the triggered-ability stack entry
 */
public record DelayedDestroyCreatureDamagedByWatchedCreature(
        UUID watchedPermanentId,
        UUID controllerId,
        Card sourceCard
) implements DelayedAction {
}
