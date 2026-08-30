package com.github.laxika.magicalvibes.model.effect;

/** Assigns a name to the targeted permanent for as long as it remains on the battlefield. */
public record SetTargetPermanentNameEffect(String name) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
