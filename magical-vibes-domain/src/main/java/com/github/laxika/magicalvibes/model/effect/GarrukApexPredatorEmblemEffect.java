package com.github.laxika.magicalvibes.model.effect;

/**
 * Garruk, Apex Predator's −8: "Target opponent gets an emblem with \"Whenever a creature attacks
 * you, it gets +5/+5 and gains trample until end of turn.\""
 *
 * <p>Unlike every other emblem effect in the engine the emblem is created under the <em>target
 * player's</em> control, not the ability controller's, so the handler reads the stack entry's
 * target player. The emblem itself carries a single
 * {@link BoostAttackingCreatureOnAttacksYouEffect} marker, which {@code CombatAttackService} scans
 * when attackers are declared.
 */
public record GarrukApexPredatorEmblemEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
