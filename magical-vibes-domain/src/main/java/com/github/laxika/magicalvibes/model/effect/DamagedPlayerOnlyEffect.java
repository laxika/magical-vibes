package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Gates a damage-to-player triggered ability on the damaged player being {@code playerId}
 * ("When this permanent deals damage to the player who cast Hellish Rebuke, ..."). The player is
 * independent of who currently controls the source. A {@code null} {@code playerId} is a placeholder that
 * the granting effect fills in with its controller when it resolves.
 */
public record DamagedPlayerOnlyEffect(UUID playerId, CardEffect wrapped) implements CardEffect {

    public DamagedPlayerOnlyEffect withPlayer(UUID newPlayerId) {
        return new DamagedPlayerOnlyEffect(newPlayerId, wrapped);
    }
}
