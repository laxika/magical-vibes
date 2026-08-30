package com.github.laxika.magicalvibes.model.effect;

/** Exiles the source card from its graveyard and records the creature it haunts. */
public record HauntEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
