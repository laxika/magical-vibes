package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Registers a one-shot delayed triggered ability for the next end step ("at the beginning of the next end step, ...",
 * CR 603.7b) whose effect is {@code triggerEffect}. It is about the objects its maker chose, or, with target groups,
 * chooses its own targets as it goes on the stack (CR 603.3d).
 *
 * @param aboutActivePlayer whether it is about the player whose turn it was as its maker resolved, "that player" of
 *                          "creatures the active player controls attack this turn if able. At the beginning of the next
 *                          end step, destroy all ... creatures that player controls ..." (Siren's Call): its entry names
 *                          that player, non-targeting, whoever's end step it triggers at
 */
public record RegisterDelayedEndStepTriggerEffect(
        List<DelayedTargetGroup> targetGroups,
        CardEffect triggerEffect,
        boolean aboutActivePlayer
) implements CardEffect {

    public RegisterDelayedEndStepTriggerEffect {
        targetGroups = List.copyOf(targetGroups);
    }

    public RegisterDelayedEndStepTriggerEffect(List<DelayedTargetGroup> targetGroups, CardEffect triggerEffect) {
        this(targetGroups, triggerEffect, false);
    }

    /** {@code triggerEffect} at the beginning of the next end step, about the active player as it is registered. */
    public static RegisterDelayedEndStepTriggerEffect aboutActivePlayer(CardEffect triggerEffect) {
        return new RegisterDelayedEndStepTriggerEffect(List.of(), triggerEffect, true);
    }
}
