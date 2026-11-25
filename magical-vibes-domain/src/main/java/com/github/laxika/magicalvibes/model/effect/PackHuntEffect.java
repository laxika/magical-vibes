package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Pack hunt effect: tap all untapped creatures of the given subtype the controller controls.
 * Each creature tapped this way deals damage equal to its power to target creature. That creature
 * deals damage equal to its power divided as its controller chooses among those tapped creatures
 * (one at a time through a permanent choice when more than one was tapped).
 *
 * <p>Used by Master of the Wild Hunt (Wolf subtype).
 */
public record PackHuntEffect(CardSubtype creatureSubtype) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
