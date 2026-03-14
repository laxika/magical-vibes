package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Pack hunt effect: tap all untapped creatures of the given subtype the controller controls.
 * Each creature tapped this way deals damage equal to its power to target creature. That creature
 * deals damage equal to its power divided evenly among those tapped creatures.
 *
 * <p>Note: the original rules text says "divided as its controller chooses" for the target
 * creature's damage back. This implementation divides evenly (with remainder distributed
 * one-by-one starting from the first creature).
 *
 * <p>Used by Master of the Wild Hunt (Wolf subtype).
 */
public record PackHuntEffect(CardSubtype creatureSubtype) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
