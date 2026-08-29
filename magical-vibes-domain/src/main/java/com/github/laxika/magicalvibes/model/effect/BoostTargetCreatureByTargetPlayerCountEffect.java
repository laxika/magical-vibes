package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Applies dynamic power and toughness modifiers to a target creature, evaluating the amounts with
 * a separately declared target player's id as the target context. This supports effects such as
 * "target creature gets -1/-1 until end of turn for each Zombie that player controls".
 */
public record BoostTargetCreatureByTargetPlayerCountEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        boolean harmful,
        int targetPlayerGroup
) implements CreatureBoostEffect {

    public BoostTargetCreatureByTargetPlayerCountEffect(DynamicAmount powerBoost,
                                                         DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, false, 0);
    }

    public BoostTargetCreatureByTargetPlayerCountEffect(DynamicAmount powerBoost,
                                                         DynamicAmount toughnessBoost,
                                                         boolean harmful) {
        this(powerBoost, toughnessBoost, harmful, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.creature(), harmful, null, false, 1);
    }
}
