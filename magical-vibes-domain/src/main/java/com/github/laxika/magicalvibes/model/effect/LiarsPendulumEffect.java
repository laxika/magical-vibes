package com.github.laxika.magicalvibes.model.effect;

/**
 * Choose a card name, have the target opponent guess whether a card with that name is in the
 * controller's hand, then offer the controller the option to reveal their hand and draw a card
 * if the guess was wrong.
 */
public record LiarsPendulumEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
