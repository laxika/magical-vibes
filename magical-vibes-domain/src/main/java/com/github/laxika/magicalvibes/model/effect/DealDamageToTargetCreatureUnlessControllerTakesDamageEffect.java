package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The targeted creature's controller chooses whether the source deals damage to them or the
 * targeted creature is dealt damage.
 */
public record DealDamageToTargetCreatureUnlessControllerTakesDamageEffect(
        DynamicAmount targetDamage, DynamicAmount controllerDamage) implements CardEffect {

    public DealDamageToTargetCreatureUnlessControllerTakesDamageEffect(int targetDamage,
                                                                         int controllerDamage) {
        this(new Fixed(targetDamage), new Fixed(controllerDamage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
