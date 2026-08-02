package com.github.laxika.magicalvibes.model.effect;

/**
 * "Destroy all creatures of the creature type of your choice." (Extinction)
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs and destroys every creature of that
 * type on any battlefield (Changeling-aware).</p>
 */
public record DestroyAllCreaturesOfChosenTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
