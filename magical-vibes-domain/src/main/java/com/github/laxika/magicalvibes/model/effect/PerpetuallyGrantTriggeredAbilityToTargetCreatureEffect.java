package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to a targeted creature. */
public record PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility,
        PermanentPredicate filter) implements CardEffect {

    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
            EffectSlot triggeredAbilitySlot, CardEffect triggeredAbility) {
        this(triggeredAbilitySlot, triggeredAbility, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }
}
