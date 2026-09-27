package com.github.laxika.magicalvibes.model.effect;

/** Marks the permanent just put onto the battlefield by a hand choice as manifested. */
public record MarkChosenPermanentAsManifestedEffect() implements CardEffect {

    @Override
    public boolean usesChosenPermanentReference() {
        return true;
    }
}
