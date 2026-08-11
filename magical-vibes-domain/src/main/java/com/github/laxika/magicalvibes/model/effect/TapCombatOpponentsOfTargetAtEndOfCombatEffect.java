package com.github.laxika.magicalvibes.model.effect;

/**
 * "At this turn's next end of combat, tap each creature that was blocked by one of those creatures
 * this turn and it doesn't untap during its controller's next untap step." The target group holds
 * the creatures whose combat opponents are checked when the delayed action resolves.
 */
public record TapCombatOpponentsOfTargetAtEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
