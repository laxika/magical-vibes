package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the ability controller control of the creature in the target group chosen by the player
 * in the first target group, for the given duration.
 *
 * <p>The first target group must contain the chosen player and the configured target group must
 * contain that player's chosen creature. The handler rechecks both relationships when the effect
 * resolves.</p>
 */
public record GainControlOfTargetCreatureOfChosenPlayerEffect(ControlDuration duration,
                                                              int targetGroup)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.player(), TargetPredicates.creature()));
    }
}
