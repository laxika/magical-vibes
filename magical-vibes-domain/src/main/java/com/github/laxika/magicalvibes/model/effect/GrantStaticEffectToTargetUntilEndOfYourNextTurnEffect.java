package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to target permanent until the end of its controller's next turn.
 *
 * @param staticEffect the static effect the target permanent gains
 */
public record GrantStaticEffectToTargetUntilEndOfYourNextTurnEffect(CardEffect staticEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
