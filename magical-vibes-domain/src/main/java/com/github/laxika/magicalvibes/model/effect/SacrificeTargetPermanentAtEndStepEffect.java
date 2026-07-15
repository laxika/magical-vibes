package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the targeted permanent to be sacrificed at the beginning of the next end step
 * (e.g. Lowland Oaf's "Sacrifice that creature at the beginning of the next end step").
 */
public record SacrificeTargetPermanentAtEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
