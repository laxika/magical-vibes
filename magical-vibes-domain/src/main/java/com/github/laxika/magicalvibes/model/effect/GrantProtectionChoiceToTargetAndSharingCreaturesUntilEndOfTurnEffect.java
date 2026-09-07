package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts for a color, then grants protection from it to the target creature and each other
 * creature that shares a color with that target. The affected creatures are determined on
 * resolution.
 */
public record GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
