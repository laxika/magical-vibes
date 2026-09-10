package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the permanent just chosen by a preceding hand-to-battlefield effect to the source
 * permanent, when the chosen permanent is an Equipment.
 */
public record AttachChosenPermanentToSourceEffect() implements CardEffect {

    @Override
    public boolean usesChosenPermanentReference() {
        return true;
    }
}
