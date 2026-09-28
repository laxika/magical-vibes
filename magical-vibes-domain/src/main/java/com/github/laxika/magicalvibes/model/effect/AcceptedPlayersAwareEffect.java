package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * A follow-up effect whose affected players are determined by a preceding multi-player choice.
 */
public interface AcceptedPlayersAwareEffect extends CardEffect {

    CardEffect withAcceptedPlayerIds(List<UUID> playerIds);
}
