package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature deals damage equal to its power to that creature's controller.
 * The target creature is the damage source; its controller is the recipient (Dong Zhou, the Tyrant).
 */
public record TargetCreatureDealsPowerDamageToControllerEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
