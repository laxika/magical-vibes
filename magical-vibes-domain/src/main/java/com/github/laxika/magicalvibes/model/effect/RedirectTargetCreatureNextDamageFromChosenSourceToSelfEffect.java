package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to target creature this turn, that damage
 * is dealt to this creature instead." The target creature is the ability's target; the source is chosen
 * on resolution (not a target); the redirected damage is dealt to the ability's source permanent. Only
 * the next single damage event from the chosen source is redirected, then the shield is consumed.
 */
public record RedirectTargetCreatureNextDamageFromChosenSourceToSelfEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
