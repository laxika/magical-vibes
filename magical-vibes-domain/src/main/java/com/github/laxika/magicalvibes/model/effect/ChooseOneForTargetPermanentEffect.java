package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * A resolution-time modal effect whose every mode applies to the same target permanent.
 *
 * @param options the modes, in card-text order
 */
public record ChooseOneForTargetPermanentEffect(List<ChooseOneEffect.ChooseOneOption> options)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
