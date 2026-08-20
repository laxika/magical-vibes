package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a creature or planeswalker on the battlefield. Destroy all other
 * creatures and planeswalkers.
 *
 * <p>The choice is mandatory when at least one matching permanent exists. Destruction respects
 * indestructible and regeneration.
 */
public record ChooseCreatureOrPlaneswalkerDestroyRestEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
