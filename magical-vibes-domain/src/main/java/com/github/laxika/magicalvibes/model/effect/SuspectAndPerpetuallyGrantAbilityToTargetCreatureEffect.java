package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/** Suspects the damaged creature and perpetually grants it this same combat-damage ability. */
public record SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect() implements CardEffect {

    public EffectSlot triggeredAbilitySlot() {
        return EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
