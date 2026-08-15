package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Each player may scry the specified number of cards, in active-player-first order.
 *
 * <p>The empty-player-list constructor is used when the effect first resolves. The handler fills
 * the list with the APNAP order and carries the remaining players through each may choice.
 */
public record EachPlayerMayScryEffect(int count, List<UUID> remainingPlayerIds) implements CardEffect {

    public EachPlayerMayScryEffect(int count) {
        this(count, List.of());
    }

    public EachPlayerMayScryEffect {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
    }
}
