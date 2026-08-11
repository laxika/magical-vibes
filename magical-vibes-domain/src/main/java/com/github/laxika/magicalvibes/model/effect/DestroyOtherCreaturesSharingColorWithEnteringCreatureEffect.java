package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys all other creatures that share a color with the creature that caused this trigger.
 * The entering creature is carried by {@code StackEntry.triggeringPermanentId} and its card id.
 */
public record DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
