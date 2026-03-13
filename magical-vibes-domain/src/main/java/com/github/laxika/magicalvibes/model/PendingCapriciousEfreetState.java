package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks the two-step target selection for Capricious Efreet's upkeep trigger.
 *
 * <p>Step 1 selects the mandatory own nonland permanent target; step 2 selects
 * up to two opponent nonland permanent targets. After both steps complete,
 * all targets are pushed onto the stack as a single triggered ability.
 *
 * @param sourceCard         the Capricious Efreet card
 * @param controllerId       the controller of the Efreet
 * @param sourcePermanentId  the permanent ID of the Efreet on the battlefield
 * @param ownTargetId        the chosen nonland permanent the controller controls
 */
public record PendingCapriciousEfreetState(
        Card sourceCard,
        UUID controllerId,
        UUID sourcePermanentId,
        UUID ownTargetId
) {
}
