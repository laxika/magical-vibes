package com.github.laxika.magicalvibes.model.effect;

/**
 * Words of War's activated ability: registers a one-shot, turn-scoped replacement of the
 * controller's next draw with 2 damage to the ability's chosen target.
 */
public record RegisterNextDrawDamageReplacementEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
