package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses up to two creatures on the battlefield. Destroy all other creatures.
 *
 * <p>The choice is made during resolution and may be declined. Destruction respects indestructible
 * and regeneration.
 */
public record ChooseUpToTwoCreaturesDestroyRestEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
