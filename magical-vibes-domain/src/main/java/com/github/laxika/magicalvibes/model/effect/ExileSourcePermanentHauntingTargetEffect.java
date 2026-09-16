package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source permanent and records that it haunts the targeted creature. The engine keeps
 * the link until the creature leaves the battlefield, then returns the exiled source card.
 */
public record ExileSourcePermanentHauntingTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
