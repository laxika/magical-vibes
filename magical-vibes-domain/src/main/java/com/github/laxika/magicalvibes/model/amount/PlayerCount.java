package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import java.util.Objects;

/**
 * The number of players {@code players} names as the effect resolves: "for each opponent, you create a token unless
 * that player …" is a token for each of {@code except(eachOpponent(), acceptedPlayers())} (Acererak the Archlich).
 * Read against the resolving stack entry; 0 without one.
 *
 * @param players the players to count
 */
public record PlayerCount(PlayerSubject players) implements DynamicAmount {

    public PlayerCount {
        Objects.requireNonNull(players, "players");
    }
}
