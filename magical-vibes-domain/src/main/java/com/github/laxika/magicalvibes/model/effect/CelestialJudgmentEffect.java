package com.github.laxika.magicalvibes.model.effect;

/**
 * For each different power among creatures on the battlefield, the controller chooses a creature
 * with that power, then destroys every creature not chosen this way.
 */
public record CelestialJudgmentEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
