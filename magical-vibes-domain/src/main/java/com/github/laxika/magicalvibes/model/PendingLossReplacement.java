package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A game loss whose replacement (The Golden Throne, Exquisite Archangel, Lich's Mirror) waits for the questions of the
 * Lich's Duel Mastery sacrifice that would replace the life loss of the life total it sets (CR 616.1f: they are
 * chosen before the replacement changes anything). Once they are answered the loss is resolved again, and the
 * replacement's sacrifice performs the answered batch.
 *
 * @param playerId the player who would lose the game
 * @param reason   the name of the loss's {@code LossReason}
 */
public record PendingLossReplacement(UUID playerId, String reason) {
}
