package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * Continuation state for {@link FlipUntilLoseOrStopEffect}. It is carried by a pending may ability
 * while the controller decides whether to flip again, and is never placed on the stack.
 *
 * @param wins    wins already achieved
 * @param rewards rewards in ascending order of their minimum win count
 */
public record FlipUntilLoseOrStopContinuationEffect(int wins, List<CardEffect> rewards) implements CardEffect {

    public FlipUntilLoseOrStopContinuationEffect {
        if (wins < 1) {
            throw new IllegalArgumentException("wins must be positive");
        }
        Objects.requireNonNull(rewards, "rewards");
        rewards = List.copyOf(rewards);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
