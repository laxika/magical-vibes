package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Carrier state for Guild Feud's two sequential reveal stages. Queued when the opponent's
 * {@code LibraryRevealChoice} begins and re-queued for the controller's stage; the answer path
 * polls it to advance, and after the controller's stage the two creatures put onto the
 * battlefield (if there are two) fight each other.
 *
 * @param controllerId                 Guild Feud's controller (the second revealer)
 * @param opponentId                   the targeted opponent (the first revealer)
 * @param controllerStage              {@code true} once the pending choice is the controller's own
 * @param opponentCreaturePermanentId  the permanent the opponent put onto the battlefield, or
 *                                     {@code null} if they put none
 */
public record PendingGuildFeud(UUID controllerId, UUID opponentId, boolean controllerStage,
                               UUID opponentCreaturePermanentId) implements PendingInteraction {
}
