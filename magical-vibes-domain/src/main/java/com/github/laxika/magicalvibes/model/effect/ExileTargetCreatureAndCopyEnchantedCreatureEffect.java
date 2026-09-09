package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Exiles the optional target creature until the source Aura leaves, then makes the Aura's
 * enchanted creature a copy of the exiled creature for as long as the Aura remains attached.
 */
public record ExileTargetCreatureAndCopyEnchantedCreatureEffect()
        implements CardEffect, OptionalTargetEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()));
    }
}
