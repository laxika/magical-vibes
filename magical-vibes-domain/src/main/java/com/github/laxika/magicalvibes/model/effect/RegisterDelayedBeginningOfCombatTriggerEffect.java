package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Registers a one-shot, targeted trigger for the next beginning of combat this turn. */
public record RegisterDelayedBeginningOfCombatTriggerEffect(
        List<DelayedTargetGroup> targetGroups,
        CardEffect triggerEffect
) implements CardEffect {

    public RegisterDelayedBeginningOfCombatTriggerEffect {
        targetGroups = List.copyOf(targetGroups);
    }
}
