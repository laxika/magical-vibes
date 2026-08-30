package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Causes the source permanent to become a copy of another target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot, boolean copyColor) implements CardEffect {

    public BecomeCopyOfTargetCreatureEffect() {
        this(EffectSlot.UPKEEP_TRIGGERED, true);
    }

    public BecomeCopyOfTargetCreatureEffect(EffectSlot retainedEffectSlot) {
        this(retainedEffectSlot, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
    }
}
