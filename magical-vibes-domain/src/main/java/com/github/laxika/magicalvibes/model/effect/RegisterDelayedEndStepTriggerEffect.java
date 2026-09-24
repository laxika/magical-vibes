package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Registers a one-shot, targeted trigger for the next end step. */
public record RegisterDelayedEndStepTriggerEffect(
        List<DelayedTargetGroup> targetGroups,
        CardEffect triggerEffect
) implements CardEffect {

    public RegisterDelayedEndStepTriggerEffect {
        targetGroups = List.copyOf(targetGroups);
    }
}
