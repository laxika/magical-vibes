package com.github.laxika.magicalvibes.model.effect;

/**
 * First targeted creature and second targeted creature deal damage to each other
 * equal to their respective powers (fight mechanic). Both use effective power at resolution.
 * Protection is checked against each creature's color rather than the spell's color.
 */
public record FirstTargetFightsSecondTargetEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
