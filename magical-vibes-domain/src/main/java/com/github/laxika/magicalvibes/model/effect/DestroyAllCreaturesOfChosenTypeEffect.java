package com.github.laxika.magicalvibes.model.effect;

/**
 * "Destroy all creatures of the creature type of your choice." (Extinction), or all creatures
 * that are not of the chosen type (Kindred Dominance).
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs and destroys the matching or
 * nonmatching creatures on any battlefield (Changeling-aware).</p>
 */
public record DestroyAllCreaturesOfChosenTypeEffect(boolean destroyNonMatching) implements BoardWipeEffect {

    public DestroyAllCreaturesOfChosenTypeEffect() {
        this(false);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
