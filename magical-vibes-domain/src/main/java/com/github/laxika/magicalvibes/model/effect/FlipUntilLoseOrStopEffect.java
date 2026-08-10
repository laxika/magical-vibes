package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * Flips coins until the controller loses a flip or chooses to stop, then resolves each reward
 * whose minimum number of wins was reached.
 *
 * @param rewards rewards in ascending order of their minimum win count; the first reward is for
 *                one or more wins, the second for two or more, and so on
 */
public record FlipUntilLoseOrStopEffect(List<CardEffect> rewards) implements CardEffect {

    public FlipUntilLoseOrStopEffect {
        Objects.requireNonNull(rewards, "rewards");
        if (rewards.isEmpty() || rewards.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one non-null reward is required");
        }
        rewards = List.copyOf(rewards);
    }

    @Override
    public TargetSpec targetSpec() {
        return rewards.stream()
                .map(CardEffect::targetSpec)
                .filter(spec -> spec.declaredTarget() != null)
                .findFirst()
                .orElse(TargetSpec.NONE);
    }
}
