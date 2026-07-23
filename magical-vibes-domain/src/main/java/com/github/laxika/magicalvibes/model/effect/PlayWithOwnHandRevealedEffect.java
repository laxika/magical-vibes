package com.github.laxika.magicalvibes.model.effect;

/**
 * Static: "Play with your hand revealed." Only the controller's hand is publicly visible
 * (contrast {@link PlayWithHandsRevealedEffect}, which reveals every player's hand). Used by
 * Enduring Renewal.
 */
public record PlayWithOwnHandRevealedEffect() implements PubliclyRevealedHandEffect {

    @Override
    public boolean controllerOnly() {
        return true;
    }
}
