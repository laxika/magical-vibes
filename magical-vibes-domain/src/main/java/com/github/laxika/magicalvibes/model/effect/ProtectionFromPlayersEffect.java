package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * Grants protection from the given players: from every spell, ability, permanent and damage source they control.
 * The players are fixed when the effect that grants it resolves (the chosen player, the target player, or each
 * opponent of the effect's controller at that moment).
 *
 * @param playerIds    the players the permanent has protection from
 * @param eachOpponent whether the granting effect named "each of your opponents" rather than one player
 */
public record ProtectionFromPlayersEffect(Set<UUID> playerIds, boolean eachOpponent)
        implements ProtectionGrantingEffect {

    public ProtectionFromPlayersEffect {
        playerIds = Set.copyOf(playerIds);
    }

    @Override
    public Set<UUID> protectionFromPlayers() {
        return playerIds;
    }
}
