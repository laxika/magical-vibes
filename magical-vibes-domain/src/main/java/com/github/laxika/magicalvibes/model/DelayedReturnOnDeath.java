package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A delayed return registered for a creature card that dies later this turn.
 *
 * @param controllerId the controller of the ability that created the delayed trigger
 * @param enterTapped whether the returned permanent enters tapped
 * @param returnUnderController whether the permanent returns under {@code controllerId}'s control
 * @param requireControllerGraveyard whether the trigger only fires if the card enters the ability
 *                                    controller's graveyard
 */
public record DelayedReturnOnDeath(UUID controllerId, boolean enterTapped, boolean returnUnderController,
                                   boolean requireControllerGraveyard) {
}
