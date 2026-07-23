package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: "Players play with their hands revealed." While any player controls a permanent
 * with this effect, every player's hand is visible to every player. Consumed by
 * {@code GameBroadcastService.getRevealedOpponentHand}. Used by Zur's Weirding.
 */
public record PlayWithHandsRevealedEffect() implements PubliclyRevealedHandEffect {

    @Override
    public boolean controllerOnly() {
        return false;
    }
}
